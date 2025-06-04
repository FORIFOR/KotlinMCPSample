package com.example.mcpsample.tools

import kotlinx.serialization.json.*
import com.example.mcpsample.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object FunctionRegistry {
    
    // サービスインスタンス
    private val weatherService = WeatherService()
    private val notionService = NotionService()
    private val memoryService = MemoryService()
    private val systemInfoService = SystemInfoService()
    
    // 🔧 修正: Gemini API に適したFunction定義
    fun getGeminiTools(): List<GeminiTool> {
        return listOf(
            GeminiTool(
                functionDeclarations = listOf(
                    // 🌤️ Weather API Tool (修正版)
                    GeminiFunctionDeclaration(
                        name = "get_weather",
                        description = "指定された都市の現在の天気情報を取得します",
                        parameters = GeminiFunctionParameters(
                            type = "object", // 🔧 小文字に修正
                            properties = buildJsonObject {
                                put("location", buildJsonObject {
                                    put("type", JsonPrimitive("string")) // 🔧 小文字に修正
                                    put("description", JsonPrimitive("天気情報を取得したい都市名（例：東京、横浜、大阪）"))
                                })
                            },
                            required = listOf("location")
                        )
                    ),
                    
                    // 📝 Notion API Tool (修正版)
                    GeminiFunctionDeclaration(
                        name = "create_notion_page",
                        description = "Notionに新しいページを作成します",
                        parameters = GeminiFunctionParameters(
                            type = "object", // 🔧 小文字に修正
                            properties = buildJsonObject {
                                put("title", buildJsonObject {
                                    put("type", JsonPrimitive("string")) // 🔧 小文字に修正
                                    put("description", JsonPrimitive("作成するページのタイトル"))
                                })
                                put("content", buildJsonObject {
                                    put("type", JsonPrimitive("string")) // 🔧 小文字に修正
                                    put("description", JsonPrimitive("ページの本文内容"))
                                })
                            },
                            required = listOf("title", "content")
                        )
                    ),
                    
                    // ⏰ Time/Date Tool (修正版)
                    GeminiFunctionDeclaration(
                        name = "get_current_time",
                        description = "現在の時刻や日付情報を取得します",
                        parameters = GeminiFunctionParameters(
                            type = "object", // 🔧 小文字に修正
                            properties = buildJsonObject {
                                put("format", buildJsonObject {
                                    put("type", JsonPrimitive("string")) // 🔧 小文字に修正
                                    put("description", JsonPrimitive("取得する情報のタイプ: time（時刻）, date（日付）, both（両方）"))
                                    put("enum", buildJsonArray {
                                        add(JsonPrimitive("time"))
                                        add(JsonPrimitive("date"))
                                        add(JsonPrimitive("both"))
                                    })
                                })
                            },
                            required = listOf("format")
                        )
                    )
                )
            )
        )
    }
    
    // ツール実行関数
    suspend fun executeFunction(name: String, args: JsonObject): JsonObject {
        return withContext(Dispatchers.IO) {
            try {
                when (name) {
                    "get_weather" -> executeWeatherFunction(args)
                    "create_notion_page" -> executeNotionFunction(args)
                    "get_current_time" -> executeTimeFunction(args)
                    "store_memory" -> executeMemoryFunction(args)
                    "get_system_info" -> executeSystemInfoFunction(args)
                    else -> buildJsonObject {
                        put("success", JsonPrimitive(false))
                        put("error", JsonPrimitive("Unknown function: $name"))
                    }
                }
            } catch (e: Exception) {
                buildJsonObject {
                    put("success", JsonPrimitive(false))
                    put("error", JsonPrimitive("Function execution error: ${e.message}"))
                    put("function", JsonPrimitive(name))
                }
            }
        }
    }
    
    // 🌤️ Weather function
    private suspend fun executeWeatherFunction(args: JsonObject): JsonObject {
        val location = args["location"]?.jsonPrimitive?.content ?: "横浜"
        
        return try {
            val weatherData = weatherService.getWeather(location)
            
            buildJsonObject {
                put("success", JsonPrimitive(true))
                put("location", JsonPrimitive(location))
                put("temperature", JsonPrimitive(weatherData.temperature))
                put("description", JsonPrimitive(weatherData.description))
                put("humidity", JsonPrimitive(weatherData.humidity))
                put("wind_speed", JsonPrimitive(weatherData.windSpeed))
                put("message", JsonPrimitive("${location}の天気: ${weatherData.description}, 気温: ${weatherData.temperature}°C, 湿度: ${weatherData.humidity}%, 風速: ${weatherData.windSpeed}m/s"))
            }
        } catch (e: Exception) {
            buildJsonObject {
                put("success", JsonPrimitive(false))
                put("error", JsonPrimitive("Weather data fetch failed: ${e.message}"))
                put("location", JsonPrimitive(location))
            }
        }
    }
    
    // 📝 Notion function
    private suspend fun executeNotionFunction(args: JsonObject): JsonObject {
        val title = args["title"]?.jsonPrimitive?.content ?: "新しいページ"
        val content = args["content"]?.jsonPrimitive?.content ?: ""
        
        return try {
            val pageId = notionService.createPage(title, content)
            
            buildJsonObject {
                put("success", JsonPrimitive(true))
                put("page_id", JsonPrimitive(pageId))
                put("title", JsonPrimitive(title))
                put("message", JsonPrimitive("Notionページ「$title」を作成しました (ID: $pageId)"))
            }
        } catch (e: Exception) {
            buildJsonObject {
                put("success", JsonPrimitive(false))
                put("error", JsonPrimitive("Notion page creation failed: ${e.message}"))
                put("title", JsonPrimitive(title))
            }
        }
    }
    
    // ⏰ Time function
    private suspend fun executeTimeFunction(args: JsonObject): JsonObject {
        val format = args["format"]?.jsonPrimitive?.content ?: "both"
        
        return try {
            val currentTime = systemInfoService.getCurrentTime()
            val currentDate = systemInfoService.getCurrentDate()
            
            val message = when (format) {
                "time" -> "現在時刻: $currentTime"
                "date" -> "今日の日付: $currentDate"
                "both" -> "現在時刻: $currentTime\n今日の日付: $currentDate"
                else -> "現在時刻: $currentTime\n今日の日付: $currentDate"
            }
            
            buildJsonObject {
                put("success", JsonPrimitive(true))
                put("time", JsonPrimitive(currentTime))
                put("date", JsonPrimitive(currentDate))
                put("format", JsonPrimitive(format))
                put("message", JsonPrimitive(message))
            }
        } catch (e: Exception) {
            buildJsonObject {
                put("success", JsonPrimitive(false))
                put("error", JsonPrimitive("Time data fetch failed: ${e.message}"))
            }
        }
    }
    
    // 🧠 Memory function
    private suspend fun executeMemoryFunction(args: JsonObject): JsonObject {
        val key = args["key"]?.jsonPrimitive?.content ?: return buildJsonObject {
            put("success", JsonPrimitive(false))
            put("error", JsonPrimitive("Key is required"))
        }
        
        val value = args["value"]?.jsonPrimitive?.content ?: return buildJsonObject {
            put("success", JsonPrimitive(false))
            put("error", JsonPrimitive("Value is required"))
        }
        
        val description = args["description"]?.jsonPrimitive?.content ?: ""
        
        return try {
            memoryService.store(key, value, description)
            
            buildJsonObject {
                put("success", JsonPrimitive(true))
                put("key", JsonPrimitive(key))
                put("value", JsonPrimitive(value))
                put("description", JsonPrimitive(description))
                put("message", JsonPrimitive("情報を「$key」として保存しました: $value"))
            }
        } catch (e: Exception) {
            buildJsonObject {
                put("success", JsonPrimitive(false))
                put("error", JsonPrimitive("Memory storage failed: ${e.message}"))
                put("key", JsonPrimitive(key))
            }
        }
    }
    
    // 📱 System Info function
    private suspend fun executeSystemInfoFunction(args: JsonObject): JsonObject {
        val type = args["type"]?.jsonPrimitive?.content ?: "basic"
        
        return try {
            val info = when (type) {
                "detailed" -> systemInfoService.getDetailedSystemInfo()
                else -> systemInfoService.getDeviceInfo()
            }
            
            buildJsonObject {
                put("success", JsonPrimitive(true))
                put("type", JsonPrimitive(type))
                put("info", JsonPrimitive(info))
                put("message", JsonPrimitive("システム情報を取得しました"))
            }
        } catch (e: Exception) {
            buildJsonObject {
                put("success", JsonPrimitive(false))
                put("error", JsonPrimitive("System info fetch failed: ${e.message}"))
                put("type", JsonPrimitive(type))
            }
        }
    }
    
    // クリーンアップ
    fun cleanup() {
        weatherService.close()
        notionService.close()
    }
}
