package com.example.mcpsample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// Gemini API Models with Function Calling Support
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val tools: List<GeminiTool>? = null,
    @SerialName("tool_config")
    val toolConfig: GeminiToolConfig? = null,
    @SerialName("generation_config")
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    @SerialName("functionCall") 
    val functionCall: GeminiFunctionCall? = null,
    @SerialName("functionResponse")
    val functionResponse: GeminiFunctionResponse? = null
)

@Serializable
data class GeminiFunctionCall(
    val name: String,
    val args: JsonObject
)

@Serializable
data class GeminiFunctionResponse(
    val name: String,
    val response: JsonObject
)

@Serializable
data class GeminiTool(
    @SerialName("function_declarations")
    val functionDeclarations: List<GeminiFunctionDeclaration>
)

@Serializable
data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: GeminiFunctionParameters? = null
)

@Serializable
data class GeminiFunctionParameters(
    val type: String,
    val properties: JsonObject? = null,
    val required: List<String>? = null
)

@Serializable
data class GeminiToolConfig(
    @SerialName("function_calling_config")
    val functionCallingConfig: GeminiFunctionCallingConfig
)

@Serializable
data class GeminiFunctionCallingConfig(
    val mode: String = "AUTO", // "AUTO", "ANY", "NONE"
    @SerialName("allowed_function_names")
    val allowedFunctionNames: List<String>? = null
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    @SerialName("top_p")
    val topP: Float? = null,
    @SerialName("top_k")
    val topK: Int? = null,
    @SerialName("max_output_tokens")
    val maxOutputTokens: Int? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
    val usageMetadata: GeminiUsageMetadata? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    @SerialName("finishReason") val finishReason: String? = null,
    val index: Int? = null
)

@Serializable
data class GeminiUsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null
)

@Serializable
data class GeminiError(
    val error: GeminiErrorDetail
)

@Serializable
data class GeminiErrorDetail(
    val code: Int,
    val message: String,
    val status: String
)
