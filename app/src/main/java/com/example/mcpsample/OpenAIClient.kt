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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put

class OpenAIClient(private val apiKey: String) {
    
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
    
    suspend fun chatCompletion(prompt: String): String {
        return try {
            println("Creating OpenAI request for: $prompt")
            
            // 手動でJSONを構築
            val requestJson = buildJsonObject {
                put("model", "gpt-4o-mini")
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("max_tokens", 1000)
                put("temperature", 0.7)
                put("stream", false)
            }
            
            println("Request JSON: $requestJson")
            
            val response: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $apiKey")
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(requestJson.toString())
            }
            
            println("Response status: ${response.status}")
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                println("Response body: $responseBody")
                
                val chatResponse: OpenAIChatResponse = json.decodeFromString(responseBody)
                chatResponse.choices.firstOrNull()?.message?.content 
                    ?: "No response from OpenAI"
            } else {
                val errorBody = response.bodyAsText()
                println("Error response body: $errorBody")
                "OpenAI API Error (${response.status.value}): $errorBody"
            }
            
        } catch (e: Exception) {
            println("Exception in chatCompletion: ${e.stackTraceToString()}")
            "Error calling OpenAI API: ${e.message}"
        }
    }
    
    suspend fun chatCompletionStream(prompt: String): Flow<String> = flow {
        try {
            val requestJson = buildJsonObject {
                put("model", "gpt-4o-mini")
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("max_tokens", 1000)
                put("temperature", 0.7)
                put("stream", true)
            }
            
            val response: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $apiKey")
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(requestJson.toString())
            }
            
            if (response.status.isSuccess()) {
                val content = response.bodyAsText()
                emit(content)
            } else {
                emit("OpenAI API Error (${response.status.value}): ${response.bodyAsText()}")
            }
            
        } catch (e: Exception) {
            emit("Error calling OpenAI API: ${e.message}")
        }
    }
    
    fun close() {
        httpClient.close()
    }
}
