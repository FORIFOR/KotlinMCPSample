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
import kotlinx.serialization.json.*

class GeminiClientWithFunctions(private val apiKey: String) {
    
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
    
    suspend fun generateContentWithTools(prompt: String): String {
        // まず基本テストを実行
        val basicTest = testBasicGemini(prompt)
        if (basicTest.startsWith("✅")) {
            // 基本動作OK、Function Callingを試行
            return testFunctionCalling(prompt)
        } else {
            return basicTest
        }
    }
    
    // 🔧 基本的なGemini APIテスト
    private suspend fun testBasicGemini(prompt: String): String {
        return try {
            println("📝 Testing basic Gemini API...")
            
            // 最小限のリクエスト
            val requestJson = """
            {
                "contents": [
                    {
                        "parts": [
                            {
                                "text": "$prompt"
                            }
                        ]
                    }
                ]
            }
            """.trimIndent()
            
            println("📤 Basic request: $requestJson")
            
            val response: HttpResponse = httpClient.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent") {
                headers {
                    append("x-goog-api-key", apiKey)
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(requestJson)
            }
            
            println("📥 Basic response status: ${response.status}")
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                println("📥 Basic response: $responseBody")
                
                val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
                val candidates = jsonResponse["candidates"]?.jsonArray
                val firstCandidate = candidates?.firstOrNull()?.jsonObject
                val content = firstCandidate?.get("content")?.jsonObject
                val parts = content?.get("parts")?.jsonArray
                val text = parts?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.content
                
                if (text != null) {
                    "✅ Gemini基本動作確認 OK:\n$text"
                } else {
                    "⚠️ レスポンス解析に失敗: $responseBody"
                }
            } else {
                val errorBody = response.bodyAsText()
                "❌ Basic Gemini test failed (${response.status.value}): $errorBody"
            }
            
        } catch (e: Exception) {
            "❌ Basic test exception: ${e.message}"
        }
    }
    
    // 🔧 Function Calling テスト
    private suspend fun testFunctionCalling(prompt: String): String {
        return try {
            println("🛠️ Testing Function Calling...")
            
            // Google公式例に近い形でFunction Callingリクエストを作成
            val functionCallingJson = """
            {
                "contents": [
                    {
                        "parts": [
                            {
                                "text": "$prompt"
                            }
                        ]
                    }
                ],
                "tools": [
                    {
                        "function_declarations": [
                            {
                                "name": "get_weather",
                                "description": "Get current weather for a city",
                                "parameters": {
                                    "type": "object",
                                    "properties": {
                                        "location": {
                                            "type": "string",
                                            "description": "City name"
                                        }
                                    },
                                    "required": ["location"]
                                }
                            }
                        ]
                    }
                ]
            }
            """.trimIndent()
            
            println("📤 Function Calling request: $functionCallingJson")
            
            val response: HttpResponse = httpClient.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent") {
                headers {
                    append("x-goog-api-key", apiKey)
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(functionCallingJson)
            }
            
            println("📥 Function Calling response status: ${response.status}")
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                println("📥 Function Calling response: $responseBody")
                
                val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
                val candidates = jsonResponse["candidates"]?.jsonArray
                val firstCandidate = candidates?.firstOrNull()?.jsonObject
                val content = firstCandidate?.get("content")?.jsonObject
                val parts = content?.get("parts")?.jsonArray
                
                // Function Callがあるかチェック
                var hasFunction = false
                val results = mutableListOf<String>()
                
                parts?.forEach { part ->
                    val partObj = part.jsonObject
                    val functionCall = partObj["functionCall"]?.jsonObject
                    val text = partObj["text"]?.jsonPrimitive?.content
                    
                    if (functionCall != null) {
                        hasFunction = true
                        val name = functionCall["name"]?.jsonPrimitive?.content ?: "unknown"
                        val args = functionCall["args"]?.jsonObject
                        results.add("🔧 Function call detected: $name with args: $args")
                        
                        // 実際にfunction実行をシミュレート
                        if (name == "get_weather") {
                            val location = args?.get("location")?.jsonPrimitive?.content ?: "Unknown"
                            results.add("🌤️ Weather for $location: 22°C, Sunny (mock data)")
                        }
                    }
                    
                    if (text != null) {
                        results.add("💬 Text response: $text")
                    }
                }
                
                if (hasFunction) {
                    "✅ Function Calling 成功!\n${results.joinToString("\n")}"
                } else {
                    "✅ Function Calling リクエスト送信成功、しかし関数呼び出しなし:\n$responseBody"
                }
                
            } else {
                val errorBody = response.bodyAsText()
                "❌ Function Calling failed (${response.status.value}): $errorBody"
            }
            
        } catch (e: Exception) {
            "❌ Function Calling exception: ${e.message}"
        }
    }
    
    // 従来のテキスト生成（Function Callingなし）
    suspend fun generateContent(prompt: String): String {
        return testBasicGemini(prompt)
    }
    
    fun close() {
        httpClient.close()
    }
}
