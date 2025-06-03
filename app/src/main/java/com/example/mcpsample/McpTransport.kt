package com.example.mcpsample

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class McpTransport(private val uri: String) {
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    
    private var session: DefaultClientWebSocketSession? = null
    private val pendingRequests = ConcurrentHashMap<String, CompletableDeferred<McpMessage>>()
    private val requestIdCounter = AtomicInteger(0)
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    private val _messageFlow = MutableSharedFlow<McpMessage>()
    val messageFlow: SharedFlow<McpMessage> = _messageFlow.asSharedFlow()
    
    private var connectionJob: Job? = null
    
    suspend fun connect(): Boolean {
        return try {
            Log.d("MCP", "Connecting to: $uri")
            
            connectionJob = CoroutineScope(Dispatchers.IO).launch {
                client.webSocket(uri) {
                    session = this
                    Log.d("MCP", "WebSocket connected")
                    
                    // Start message handling
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val messageText = frame.readText()
                            Log.d("MCP", "Received: $messageText")
                            
                            try {
                                val message = json.decodeFromString<McpMessage>(messageText)
                                handleIncomingMessage(message)
                            } catch (e: Exception) {
                                Log.e("MCP", "Failed to parse message: $messageText", e)
                            }
                        }
                    }
                }
            }
            
            // Wait a bit for connection to establish
            delay(1000)
            session != null
        } catch (e: Exception) {
            Log.e("MCP", "Failed to connect", e)
            false
        }
    }
    
    private fun handleIncomingMessage(message: McpMessage) {
        if (message.id != null) {
            // This is a response to our request
            val pending = pendingRequests.remove(message.id)
            pending?.complete(message)
        } else {
            // This is a notification or server-initiated message
            CoroutineScope(Dispatchers.Main).launch {
                _messageFlow.emit(message)
            }
        }
    }
    
    suspend fun sendRequest(method: String, params: JsonObject? = null): McpMessage? {
        val session = this.session ?: return null
        
        val requestId = "req_${requestIdCounter.incrementAndGet()}"
        val request = McpMessage(
            id = requestId,
            method = method,
            params = params
        )
        
        val deferred = CompletableDeferred<McpMessage>()
        pendingRequests[requestId] = deferred
        
        return try {
            val messageText = json.encodeToString(McpMessage.serializer(), request)
            Log.d("MCP", "Sending: $messageText")
            
            session.send(Frame.Text(messageText))
            
            // Wait for response with timeout
            withTimeout(30000) {
                deferred.await()
            }
        } catch (e: Exception) {
            Log.e("MCP", "Failed to send request", e)
            pendingRequests.remove(requestId)
            null
        }
    }
    
    suspend fun sendNotification(method: String, params: JsonObject? = null) {
        val session = this.session ?: return
        
        val notification = McpMessage(
            method = method,
            params = params
        )
        
        try {
            val messageText = json.encodeToString(McpMessage.serializer(), notification)
            Log.d("MCP", "Sending notification: $messageText")
            session.send(Frame.Text(messageText))
        } catch (e: Exception) {
            Log.e("MCP", "Failed to send notification", e)
        }
    }
    
    suspend fun disconnect() {
        try {
            connectionJob?.cancel()
            session?.close()
            session = null
            pendingRequests.clear()
            client.close()
            Log.d("MCP", "Disconnected from MCP server")
        } catch (e: Exception) {
            Log.e("MCP", "Error during disconnect", e)
        }
    }
    
    fun isConnected(): Boolean = session != null
}
