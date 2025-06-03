package com.example.mcpsample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap

class McpClientManager {
    private val clients = ConcurrentHashMap<ServerType, McpClient>()
    private val transports = ConcurrentHashMap<ServerType, McpTransport>()
    private var isConnected: Boolean = false
    
    suspend fun connectToNotionServer(): Boolean {
        return connectToServer(ServerType.NOTION)
    }
    
    suspend fun connectToFileSystemServer(): Boolean {
        return connectToServer(ServerType.FILE_SYSTEM)
    }
    
    suspend fun connectToMemoryServer(): Boolean {
        return connectToServer(ServerType.MEMORY)
    }
    
    private suspend fun connectToServer(serverType: ServerType): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val config = McpServerRegistry.getServerByType(serverType)
                if (config == null) {
                    Log.e("MCP", "No configuration found for server type: $serverType")
                    return@withContext false
                }
                
                Log.d("MCP", "Connecting to ${config.name} at ${config.uri}")
                
                val transport = McpTransport(config.uri)
                val client = McpClient(transport)
                
                val connected = client.connect()
                if (!connected) {
                    Log.e("MCP", "Failed to connect to ${config.name}")
                    return@withContext false
                }
                
                val initialized = client.initialize()
                if (!initialized) {
                    Log.e("MCP", "Failed to initialize client for ${config.name}")
                    client.disconnect()
                    return@withContext false
                }
                
                transports[serverType] = transport
                clients[serverType] = client
                isConnected = true
                
                Log.d("MCP", "Successfully connected and initialized ${config.name}")
                true
                
            } catch (e: Exception) {
                Log.e("MCP", "Failed to connect to server: $serverType", e)
                false
            }
        }
    }
    
    fun disconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clients.values.forEach { client ->
                    try {
                        client.disconnect()
                    } catch (e: Exception) {
                        Log.e("MCP", "Error disconnecting client", e)
                    }
                }
                
                clients.clear()
                transports.clear()
                isConnected = false
                
                Log.d("MCP", "All MCP clients disconnected")
            } catch (e: Exception) {
                Log.e("MCP", "Error during disconnect", e)
            }
        }
    }
    
    suspend fun listResources(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val allResources = mutableListOf<String>()
                
                clients.forEach { (serverType, client) ->
                    try {
                        val resources = client.listResources()
                        resources.forEach { resource ->
                            allResources.add("[${serverType.name}] ${resource.name}: ${resource.description ?: resource.uri}")
                        }
                    } catch (e: Exception) {
                        Log.e("MCP", "Error listing resources from $serverType", e)
                        allResources.add("[${serverType.name}] Error: ${e.message}")
                    }
                }
                
                Log.d("MCP", "Listed ${allResources.size} total resources")
                allResources
            } catch (e: Exception) {
                Log.e("MCP", "Error listing resources", e)
                emptyList()
            }
        }
    }
    
    suspend fun callTool(name: String, arguments: Map<String, Any>): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("MCP", "Calling tool: $name with arguments: $arguments")
                
                // Convert arguments to JsonObject
                val jsonArgs = buildJsonObject {
                    arguments.forEach { (key, value) ->
                        when (value) {
                            is String -> put(key, JsonPrimitive(value))
                            is Number -> put(key, JsonPrimitive(value))
                            is Boolean -> put(key, JsonPrimitive(value))
                            else -> put(key, JsonPrimitive(value.toString()))
                        }
                    }
                }
                
                // Try each connected client until we find one that has the tool
                for ((serverType, client) in clients) {
                    try {
                        val tools = client.listTools()
                        val tool = tools.find { it.name == name }
                        
                        if (tool != null) {
                            Log.d("MCP", "Found tool '$name' on server: $serverType")
                            val result = client.callTool(name, jsonArgs)
                            
                            return@withContext if (result != null) {
                                if (result.isError) {
                                    "Error from $serverType: ${result.content.firstOrNull()?.text ?: "Unknown error"}"
                                } else {
                                    result.content.firstOrNull()?.text ?: "No result content"
                                }
                            } else {
                                "No result from tool '$name' on $serverType"
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MCP", "Error calling tool on $serverType", e)
                        continue
                    }
                }
                
                "Tool '$name' not found on any connected server"
                
            } catch (e: Exception) {
                Log.e("MCP", "Error calling tool: $name", e)
                "Error calling tool: ${e.message}"
            }
        }
    }
    
    suspend fun readResource(uri: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("MCP", "Reading resource: $uri")
                
                // Try each connected client to find the resource
                for ((serverType, client) in clients) {
                    try {
                        val resources = client.listResources()
                        val resource = resources.find { it.uri == uri }
                        
                        if (resource != null) {
                            Log.d("MCP", "Found resource '$uri' on server: $serverType")
                            val contents = client.readResource(uri)
                            return@withContext contents.firstOrNull()?.text ?: "No content"
                        }
                    } catch (e: Exception) {
                        Log.e("MCP", "Error reading resource from $serverType", e)
                        continue
                    }
                }
                
                "Resource '$uri' not found on any connected server"
                
            } catch (e: Exception) {
                Log.e("MCP", "Error reading resource: $uri", e)
                "Error reading resource: ${e.message}"
            }
        }
    }
    
    suspend fun getAvailableTools(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val allTools = mutableListOf<String>()
                
                clients.forEach { (serverType, client) ->
                    try {
                        val tools = client.listTools()
                        tools.forEach { tool ->
                            allTools.add("[${serverType.name}] ${tool.name}: ${tool.description}")
                        }
                    } catch (e: Exception) {
                        Log.e("MCP", "Error listing tools from $serverType", e)
                    }
                }
                
                allTools
            } catch (e: Exception) {
                Log.e("MCP", "Error getting available tools", e)
                emptyList()
            }
        }
    }
    
    fun getConnectedServers(): List<String> {
        return clients.keys.map { it.name }
    }
    
    fun isConnected(): Boolean = isConnected && clients.isNotEmpty()
    
    fun getConnectionCount(): Int = clients.size
}
