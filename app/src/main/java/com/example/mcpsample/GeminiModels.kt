package com.example.mcpsample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Gemini API Models
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    @SerialName("finishReason") val finishReason: String? = null
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
