package com.example.mcpsample

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class GeminiClient(private val apiKey: String) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        prettyPrint = true
    }
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    suspend fun generateContent(prompt: String): String {
        return try {
            println("Creating Gemini request for: $prompt")
            
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt)
                        )
                    )
                )
            )
            
            println("Request: $request")
            
            val response: HttpResponse = httpClient.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent") {
                headers {
                    append("x-goog-api-key", apiKey)
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(request)
            }
            
            println("Response status: ${response.status}")
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                println("Response body: $responseBody")
                
                val geminiResponse: GeminiResponse = json.decodeFromString(responseBody)
                geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "No response from Gemini"
            } else {
                val errorBody = response.bodyAsText()
                println("Error response body: $errorBody")
                "Gemini API Error (${response.status.value}): $errorBody"
            }
            
        } catch (e: Exception) {
            println("Exception in generateContent: ${e.stackTraceToString()}")
            "Error calling Gemini API: ${e.message}"
        }
    }
    
    fun close() {
        httpClient.close()
    }
}
