package com.example.mcpsample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive

class McpServerManager {
    private var mcpServer: McpServer? = null
    private var isRunning: Boolean = false
    private var connectedClients: Int = 0
    
    suspend fun startServer(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("MCP", "Starting MCP Server")
                
                mcpServer = McpServer()
                val started = mcpServer!!.start()
                
                if (started) {
                    isRunning = true
                    Log.d("MCP", "MCP Server started successfully")
                    setupServerTools()
                    true
                } else {
                    Log.e("MCP", "Failed to start MCP Server")
                    false
                }
            } catch (e: Exception) {
                Log.e("MCP", "Failed to start MCP server", e)
                false
            }
        }
    }
    
    private suspend fun setupServerTools() {
        val server = mcpServer ?: return
        
        try {
            Log.d("MCP", "Setting up server tools")
            
            // Additional custom tools can be registered here
            // The server already has default tools from McpServer.setupDefaultTools()
            
            Log.d("MCP", "Server tools setup completed")
            Log.d("MCP", "Available tools: ${server.getTools().map { it.name }}")
            Log.d("MCP", "Available resources: ${server.getResources().map { it.name }}")
            
        } catch (e: Exception) {
            Log.e("MCP", "Failed to setup server tools", e)
        }
    }
    
    suspend fun connectTransport(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // In a real implementation, this would set up WebSocket server
                // or other transport mechanisms for clients to connect
                Log.d("MCP", "Transport layer ready for connections")
                true
            } catch (e: Exception) {
                Log.e("MCP", "Failed to setup transport", e)
                false
            }
        }
    }
    
    fun stopServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mcpServer?.stop()
                mcpServer = null
                isRunning = false
                connectedClients = 0
                Log.d("MCP", "MCP Server stopped")
            } catch (e: Exception) {
                Log.e("MCP", "Error stopping MCP server", e)
            }
        }
    }
    
    suspend fun getAvailableTools(): List<Tool> {
        return withContext(Dispatchers.IO) {
            mcpServer?.getTools() ?: emptyList()
        }
    }
    
    suspend fun getAvailableResources(): List<Resource> {
        return withContext(Dispatchers.IO) {
            mcpServer?.getResources() ?: emptyList()
        }
    }
    
    suspend fun executeServerTool(name: String, arguments: Map<String, Any>): CallToolResult? {
        return withContext(Dispatchers.IO) {
            try {
                val server = mcpServer ?: return@withContext null
                
                // Convert arguments to JsonObject
                val jsonArgs = kotlinx.serialization.json.buildJsonObject {
                    arguments.forEach { (key, value) ->
                        when (value) {
                            is String -> put(key, kotlinx.serialization.json.JsonPrimitive(value))
                            is Number -> put(key, kotlinx.serialization.json.JsonPrimitive(value))
                            is Boolean -> put(key, kotlinx.serialization.json.JsonPrimitive(value))
                            else -> put(key, kotlinx.serialization.json.JsonPrimitive(value.toString()))
                        }
                    }
                }
                
                server.callTool(name, jsonArgs)
            } catch (e: Exception) {
                Log.e("MCP", "Error executing server tool: $name", e)
                CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Error executing tool: ${e.message}"
                        )
                    ),
                    isError = true
                )
            }
        }
    }
    
    suspend fun readServerResource(uri: String): List<Content> {
        return withContext(Dispatchers.IO) {
            try {
                val server = mcpServer ?: return@withContext emptyList()
                server.readResource(uri)
            } catch (e: Exception) {
                Log.e("MCP", "Error reading server resource: $uri", e)
                listOf(
                    Content(
                        type = "text",
                        text = "Error reading resource: ${e.message}"
                    )
                )
            }
        }
    }
    
    fun getServerStatus(): String {
        return if (isRunning) "Running" else "Stopped"
    }
    
    fun getConnectedClients(): Int {
        return connectedClients
    }
    
    fun isRunning(): Boolean = isRunning
    
    fun getServerInfo(): String {
        val server = mcpServer
        return if (server != null && isRunning) {
            val tools = server.getTools()
            val resources = server.getResources()
            "MCP Server Running\nTools: ${tools.size}\nResources: ${resources.size}\nClients: $connectedClients"
        } else {
            "MCP Server Stopped"
        }
    }
}
