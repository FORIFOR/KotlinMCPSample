package com.example.mcpsample

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap

class McpServer {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    private val registeredTools = ConcurrentHashMap<String, Tool>()
    private val registeredResources = ConcurrentHashMap<String, Resource>()
    private val toolHandlers = ConcurrentHashMap<String, suspend (JsonObject?) -> CallToolResult>()
    private val resourceHandlers = ConcurrentHashMap<String, suspend () -> List<Content>>()
    
    private var isRunning = false
    
    fun start(): Boolean {
        return try {
            Log.d("MCP", "Starting MCP Server")
            setupDefaultTools()
            setupDefaultResources()
            isRunning = true
            Log.d("MCP", "MCP Server started successfully")
            true
        } catch (e: Exception) {
            Log.e("MCP", "Failed to start MCP server", e)
            false
        }
    }
    
    private fun setupDefaultTools() {
        // Weather tool
        registerTool(
            Tool(
                name = "get_weather",
                description = "Get current weather information for a location",
                inputSchema = buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put("properties", buildJsonObject {
                        put("location", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("The location to get weather for"))
                        })
                    })
                    put("required", buildJsonArray {
                        add(JsonPrimitive("location"))
                    })
                }
            )
        ) { args ->
            val location = args?.get("location")?.jsonPrimitive?.content ?: "Unknown"
            Log.d("MCP", "Getting weather for: $location")
            
            // In production, you would call a real weather API
            CallToolResult(
                content = listOf(
                    Content(
                        type = "text",
                        text = "Weather in $location: Temperature 22°C, Clear skies, Humidity 65%, Wind 3 m/s"
                    )
                )
            )
        }
        
        // File operations tool
        registerTool(
            Tool(
                name = "file_operations",
                description = "Perform file system operations",
                inputSchema = buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put("properties", buildJsonObject {
                        put("operation", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("enum", buildJsonArray {
                                add(JsonPrimitive("read"))
                                add(JsonPrimitive("write"))
                                add(JsonPrimitive("list"))
                                add(JsonPrimitive("delete"))
                            })
                        })
                        put("path", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("File or directory path"))
                        })
                        put("content", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Content to write (for write operations)"))
                        })
                    })
                    put("required", buildJsonArray {
                        add(JsonPrimitive("operation"))
                        add(JsonPrimitive("path"))
                    })
                }
            )
        ) { args ->
            val operation = args?.get("operation")?.jsonPrimitive?.content ?: "read"
            val path = args?.get("path")?.jsonPrimitive?.content ?: "/"
            val content = args?.get("content")?.jsonPrimitive?.content
            
            Log.d("MCP", "File operation: $operation on $path")
            
            // In production, implement actual file operations with proper security
            when (operation) {
                "read" -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text", 
                            text = "File content from $path: Sample file content..."
                        )
                    )
                )
                "write" -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Successfully wrote content to $path"
                        )
                    )
                )
                "list" -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Directory listing for $path:\\n- file1.txt\\n- file2.txt\\n- subdirectory/"
                        )
                    )
                )
                "delete" -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Successfully deleted $path"
                        )
                    )
                )
                else -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Unknown operation: $operation"
                        )
                    ),
                    isError = true
                )
            }
        }
        
        // Memory operations tool
        registerTool(
            Tool(
                name = "memory_operations",
                description = "Store and retrieve information from memory",
                inputSchema = buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put("properties", buildJsonObject {
                        put("operation", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("enum", buildJsonArray {
                                add(JsonPrimitive("store"))
                                add(JsonPrimitive("retrieve"))
                                add(JsonPrimitive("search"))
                                add(JsonPrimitive("delete"))
                            })
                        })
                        put("key", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Memory key"))
                        })
                        put("value", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Value to store"))
                        })
                        put("query", buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Search query"))
                        })
                    })
                    put("required", buildJsonArray {
                        add(JsonPrimitive("operation"))
                    })
                }
            )
        ) { args ->
            val operation = args?.get("operation")?.jsonPrimitive?.content ?: "retrieve"
            val key = args?.get("key")?.jsonPrimitive?.content
            val value = args?.get("value")?.jsonPrimitive?.content
            val query = args?.get("query")?.jsonPrimitive?.content
            
            Log.d("MCP", "Memory operation: $operation")
            
            // In production, implement actual memory storage (database, etc.)
            when (operation) {
                "store" -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Stored value for key '$key': $value"
                        )
                    )
                )
                "retrieve" -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Retrieved value for key '$key': Sample stored data"
                        )
                    )
                )
                "search" -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Search results for '$query':\\n- Result 1\\n- Result 2\\n- Result 3"
                        )
                    )
                )
                "delete" -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Deleted key '$key' from memory"
                        )
                    )
                )
                else -> CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Unknown memory operation: $operation"
                        )
                    ),
                    isError = true
                )
            }
        }
    }
    
    private fun setupDefaultResources() {
        // System information resource
        registerResource(
            Resource(
                uri = "system://info",
                name = "System Information",
                description = "Current system information and status",
                mimeType = "application/json"
            )
        ) {
            listOf(
                Content(
                    type = "text",
                    text = """
                    {
                        "system": "Android MCP Server",
                        "version": "1.0.0",
                        "status": "running",
                        "uptime": "5 minutes",
                        "tools": ${registeredTools.size},
                        "resources": ${registeredResources.size}
                    }
                    """.trimIndent(),
                    mimeType = "application/json"
                )
            )
        }
        
        // Configuration resource
        registerResource(
            Resource(
                uri = "config://settings",
                name = "Server Configuration",
                description = "Current server configuration settings",
                mimeType = "application/json"
            )
        ) {
            listOf(
                Content(
                    type = "text",
                    text = """
                    {
                        "server": {
                            "name": "Android MCP Server",
                            "version": "1.0.0",
                            "features": ["tools", "resources", "logging"],
                            "transport": "websocket"
                        }
                    }
                    """.trimIndent(),
                    mimeType = "application/json"
                )
            )
        }
    }
    
    fun registerTool(tool: Tool, handler: suspend (JsonObject?) -> CallToolResult) {
        registeredTools[tool.name] = tool
        toolHandlers[tool.name] = handler
        Log.d("MCP", "Registered tool: ${tool.name}")
    }
    
    fun registerResource(resource: Resource, handler: suspend () -> List<Content>) {
        registeredResources[resource.uri] = resource
        resourceHandlers[resource.uri] = handler
        Log.d("MCP", "Registered resource: ${resource.uri}")
    }
    
    fun getTools(): List<Tool> = registeredTools.values.toList()
    
    fun getResources(): List<Resource> = registeredResources.values.toList()
    
    suspend fun callTool(name: String, arguments: JsonObject?): CallToolResult {
        val handler = toolHandlers[name]
        return if (handler != null) {
            try {
                handler(arguments)
            } catch (e: Exception) {
                Log.e("MCP", "Error executing tool: $name", e)
                CallToolResult(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Error executing tool '$name': ${e.message}"
                        )
                    ),
                    isError = true
                )
            }
        } else {
            CallToolResult(
                content = listOf(
                    Content(
                        type = "text",
                        text = "Tool not found: $name"
                    )
                ),
                isError = true
            )
        }
    }
    
    suspend fun readResource(uri: String): List<Content> {
        val handler = resourceHandlers[uri]
        return if (handler != null) {
            try {
                handler()
            } catch (e: Exception) {
                Log.e("MCP", "Error reading resource: $uri", e)
                listOf(
                    Content(
                        type = "text",
                        text = "Error reading resource '$uri': ${e.message}"
                    )
                )
            }
        } else {
            listOf(
                Content(
                    type = "text",
                    text = "Resource not found: $uri"
                )
            )
        }
    }
    
    fun stop() {
        try {
            isRunning = false
            registeredTools.clear()
            registeredResources.clear()
            toolHandlers.clear()
            resourceHandlers.clear()
            Log.d("MCP", "MCP Server stopped")
        } catch (e: Exception) {
            Log.e("MCP", "Error stopping MCP server", e)
        }
    }
    
    fun isRunning(): Boolean = isRunning
    
    fun getStatus(): String = if (isRunning) "Running" else "Stopped"
}