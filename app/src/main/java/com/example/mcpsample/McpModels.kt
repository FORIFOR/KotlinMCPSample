package com.example.mcpsample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// MCP Protocol Models
@Serializable
data class McpMessage(
    val jsonrpc: String = "2.0",
    val id: String? = null,
    val method: String? = null,
    val params: JsonObject? = null,
    val result: JsonElement? = null,
    val error: McpError? = null
)

@Serializable
data class McpError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null
)

@Serializable
data class InitializeRequest(
    val protocolVersion: String = "2024-11-05",
    val capabilities: ClientCapabilities,
    val clientInfo: ClientInfo
)

@Serializable
data class ClientCapabilities(
    val roots: RootsCapability? = null,
    val sampling: SamplingCapability? = null
)

@Serializable
data class RootsCapability(
    val listChanged: Boolean = true
)

@Serializable
data class SamplingCapability(
    val enabled: Boolean = true
)

@Serializable
data class ClientInfo(
    val name: String,
    val version: String
)

@Serializable
data class InitializeResult(
    val protocolVersion: String,
    val capabilities: ServerCapabilities,
    val serverInfo: ServerInfo
)

@Serializable
data class ServerCapabilities(
    val logging: LoggingCapability? = null,
    val prompts: PromptsCapability? = null,
    val resources: ResourcesCapability? = null,
    val tools: ToolsCapability? = null
)

@Serializable
data class LoggingCapability(
    val enabled: Boolean = true
)

@Serializable
data class PromptsCapability(
    val listChanged: Boolean = true
)

@Serializable
data class ResourcesCapability(
    val subscribe: Boolean = true,
    val listChanged: Boolean = true
)

@Serializable
data class ToolsCapability(
    val listChanged: Boolean = true
)

@Serializable
data class ServerInfo(
    val name: String,
    val version: String
)

@Serializable
data class Resource(
    val uri: String,
    val name: String,
    val description: String? = null,
    val mimeType: String? = null
)

@Serializable
data class Tool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)

@Serializable
data class ToolCall(
    val name: String,
    val arguments: JsonObject
)

@Serializable
data class ToolResult(
    val content: List<Content>,
    val isError: Boolean = false
)

@Serializable
data class Content(
    val type: String,
    val text: String? = null,
    val data: String? = null,
    val mimeType: String? = null
)

@Serializable
data class ListResourcesResult(
    val resources: List<Resource>
)

@Serializable
data class ListToolsResult(
    val tools: List<Tool>
)

@Serializable
data class ReadResourceRequest(
    val uri: String
)

@Serializable
data class ReadResourceResult(
    val contents: List<Content>
)

@Serializable
data class CallToolRequest(
    val name: String,
    val arguments: JsonObject? = null
)

@Serializable
data class CallToolResult(
    val content: List<Content>,
    val isError: Boolean = false
)
