package com.example.mcpsample

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

class McpClient(private val transport: McpTransport) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    private var isInitialized = false
    private var serverCapabilities: ServerCapabilities? = null
    
    suspend fun connect(): Boolean {
        return transport.connect()
    }
    
    suspend fun initialize(): Boolean {
        if (!transport.isConnected()) {
            Log.e("MCP", "Transport not connected")
            return false
        }
        
        return try {
            val initRequest = InitializeRequest(
                capabilities = ClientCapabilities(
                    roots = RootsCapability(),
                    sampling = SamplingCapability()
                ),
                clientInfo = ClientInfo(
                    name = "Android MCP Client",
                    version = "1.0.0"
                )
            )
            
            val params = json.encodeToJsonElement(initRequest) as JsonObject
            val response = transport.sendRequest("initialize", params)
            
            if (response?.error != null) {
                Log.e("MCP", "Initialize error: ${response.error}")
                return false
            }
            
            response?.result?.let { result ->
                val initResult = json.decodeFromJsonElement<InitializeResult>(result)
                serverCapabilities = initResult.capabilities
                isInitialized = true
                
                Log.d("MCP", "Initialized with server: ${initResult.serverInfo.name}")
                Log.d("MCP", "Server capabilities: $serverCapabilities")
                
                // Send initialized notification
                transport.sendNotification("notifications/initialized")
                
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.e("MCP", "Failed to initialize", e)
            false
        }
    }
    
    suspend fun listResources(): List<Resource> {
        if (!isInitialized) {
            Log.e("MCP", "Client not initialized")
            return emptyList()
        }
        
        return try {
            val response = transport.sendRequest("resources/list")
            
            if (response?.error != null) {
                Log.e("MCP", "List resources error: ${response.error}")
                return emptyList()
            }
            
            response?.result?.let { result ->
                val listResult = json.decodeFromJsonElement<ListResourcesResult>(result)
                Log.d("MCP", "Found ${listResult.resources.size} resources")
                return listResult.resources
            }
            
            emptyList()
        } catch (e: Exception) {
            Log.e("MCP", "Failed to list resources", e)
            emptyList()
        }
    }
    
    suspend fun listTools(): List<Tool> {
        if (!isInitialized) {
            Log.e("MCP", "Client not initialized")
            return emptyList()
        }
        
        return try {
            val response = transport.sendRequest("tools/list")
            
            if (response?.error != null) {
                Log.e("MCP", "List tools error: ${response.error}")
                return emptyList()
            }
            
            response?.result?.let { result ->
                val listResult = json.decodeFromJsonElement<ListToolsResult>(result)
                Log.d("MCP", "Found ${listResult.tools.size} tools")
                return listResult.tools
            }
            
            emptyList()
        } catch (e: Exception) {
            Log.e("MCP", "Failed to list tools", e)
            emptyList()
        }
    }
    
    suspend fun readResource(uri: String): List<Content> {
        if (!isInitialized) {
            Log.e("MCP", "Client not initialized")
            return emptyList()
        }
        
        return try {
            val request = ReadResourceRequest(uri)
            val params = json.encodeToJsonElement(request) as JsonObject
            val response = transport.sendRequest("resources/read", params)
            
            if (response?.error != null) {
                Log.e("MCP", "Read resource error: ${response.error}")
                return emptyList()
            }
            
            response?.result?.let { result ->
                val readResult = json.decodeFromJsonElement<ReadResourceResult>(result)
                Log.d("MCP", "Read resource: $uri")
                return readResult.contents
            }
            
            emptyList()
        } catch (e: Exception) {
            Log.e("MCP", "Failed to read resource: $uri", e)
            emptyList()
        }
    }
    
    suspend fun callTool(name: String, arguments: JsonObject? = null): CallToolResult? {
        if (!isInitialized) {
            Log.e("MCP", "Client not initialized")
            return null
        }
        
        return try {
            val request = CallToolRequest(name, arguments)
            val params = json.encodeToJsonElement(request) as JsonObject
            val response = transport.sendRequest("tools/call", params)
            
            if (response?.error != null) {
                Log.e("MCP", "Call tool error: ${response.error}")
                return CallToolResult(
                    content = listOf(Content(type = "text", text = "Error: ${response.error.message}")),
                    isError = true
                )
            }
            
            response?.result?.let { result ->
                val toolResult = json.decodeFromJsonElement<CallToolResult>(result)
                Log.d("MCP", "Tool call result: $name")
                return toolResult
            }
            
            null
        } catch (e: Exception) {
            Log.e("MCP", "Failed to call tool: $name", e)
            CallToolResult(
                content = listOf(Content(type = "text", text = "Error: ${e.message}")),
                isError = true
            )
        }
    }
    
    suspend fun disconnect() {
        transport.disconnect()
        isInitialized = false
        serverCapabilities = null
    }
    
    fun isConnected(): Boolean = transport.isConnected()
    fun isInitialized(): Boolean = isInitialized
    fun getServerCapabilities(): ServerCapabilities? = serverCapabilities
}
