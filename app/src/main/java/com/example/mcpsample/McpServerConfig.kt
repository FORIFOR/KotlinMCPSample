package com.example.mcpsample

data class McpServerConfig(
    val name: String,
    val uri: String,
    val description: String,
    val type: ServerType
)

enum class ServerType {
    NOTION,
    FILE_SYSTEM,
    MEMORY,
    WEATHER,
    CUSTOM
}

object McpServerRegistry {
    // Default MCP server configurations
    // In production, these would be configurable by the user
    val servers = listOf(
        McpServerConfig(
            name = "Notion Server",
            uri = "ws://localhost:3001/mcp",
            description = "MCP server for Notion integration",
            type = ServerType.NOTION
        ),
        McpServerConfig(
            name = "File System Server", 
            uri = "ws://localhost:3002/mcp",
            description = "MCP server for file system operations",
            type = ServerType.FILE_SYSTEM
        ),
        McpServerConfig(
            name = "Memory Server",
            uri = "ws://localhost:3003/mcp", 
            description = "MCP server for memory and knowledge management",
            type = ServerType.MEMORY
        ),
        McpServerConfig(
            name = "Weather Server",
            uri = "ws://localhost:3004/mcp",
            description = "MCP server for weather information",
            type = ServerType.WEATHER
        )
    )
    
    fun getServerByType(type: ServerType): McpServerConfig? {
        return servers.find { it.type == type }
    }
    
    fun getServerByName(name: String): McpServerConfig? {
        return servers.find { it.name == name }
    }
}
