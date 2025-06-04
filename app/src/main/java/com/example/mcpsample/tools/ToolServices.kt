package com.example.mcpsample.tools

import kotlinx.serialization.Serializable
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.os.Build
import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

// Weather Service
@Serializable
data class WeatherData(
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double
)

class WeatherService {
    private val httpClient = HttpClient(CIO)
    
    suspend fun getWeather(location: String): WeatherData {
        return try {
            // Open-Meteo APIを使用 (APIキー不要)
            val response = httpClient.get("https://api.open-meteo.com/v1/forecast") {
                parameter("latitude", getLatitude(location))
                parameter("longitude", getLongitude(location))
                parameter("current", "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code")
                parameter("timezone", "Asia/Tokyo")
            }
            
            if (response.status.value == 200) {
                val responseBody = response.bodyAsText()
                parseWeatherResponse(responseBody, location)
            } else {
                // フォールバック: モックデータ
                getMockWeatherData(location)
            }
        } catch (e: Exception) {
            // エラー時のフォールバック
            getMockWeatherData(location)
        }
    }
    
    private fun parseWeatherResponse(responseBody: String, location: String): WeatherData {
        return try {
            val json = Json.parseToJsonElement(responseBody).jsonObject
            val current = json["current"]?.jsonObject
            
            val temperature = current?.get("temperature_2m")?.jsonPrimitive?.double ?: 20.0
            val humidity = current?.get("relative_humidity_2m")?.jsonPrimitive?.int ?: 60
            val windSpeed = current?.get("wind_speed_10m")?.jsonPrimitive?.double ?: 5.0
            val weatherCode = current?.get("weather_code")?.jsonPrimitive?.int ?: 0
            
            WeatherData(
                temperature = temperature,
                description = getWeatherDescription(weatherCode),
                humidity = humidity,
                windSpeed = windSpeed
            )
        } catch (e: Exception) {
            getMockWeatherData(location)
        }
    }
    
    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "快晴"
            1 -> "晴れ"
            2 -> "一部曇り"
            3 -> "曇り"
            45, 48 -> "霧"
            51, 53, 55 -> "小雨"
            61, 63, 65 -> "雨"
            71, 73, 75 -> "雪"
            77 -> "みぞれ"
            80, 81, 82 -> "にわか雨"
            85, 86 -> "にわか雪"
            95 -> "雷雨"
            96, 99 -> "雹を伴う雷雨"
            else -> "不明"
        }
    }
    
    private fun getLatitude(location: String): Double {
        return when (location.lowercase()) {
            "東京", "tokyo" -> 35.6762
            "横浜", "yokohama" -> 35.4437
            "大阪", "osaka" -> 34.6937
            "名古屋", "nagoya" -> 35.1815
            "札幌", "sapporo" -> 43.0642
            "福岡", "fukuoka" -> 33.5904
            else -> 35.6762 // デフォルトは東京
        }
    }
    
    private fun getLongitude(location: String): Double {
        return when (location.lowercase()) {
            "東京", "tokyo" -> 139.6503
            "横浜", "yokohama" -> 139.6380
            "大阪", "osaka" -> 135.5023
            "名古屋", "nagoya" -> 136.9066
            "札幌", "sapporo" -> 141.3469
            "福岡", "fukuoka" -> 130.4017
            else -> 139.6503 // デフォルトは東京
        }
    }
    
    private fun getMockWeatherData(location: String): WeatherData {
        // ランダムなモックデータを生成
        val random = Random()
        return WeatherData(
            temperature = 15.0 + random.nextDouble() * 20.0, // 15-35度
            description = listOf("晴れ", "曇り", "小雨", "快晴")[random.nextInt(4)],
            humidity = 30 + random.nextInt(50), // 30-80%
            windSpeed = random.nextDouble() * 10.0 // 0-10 m/s
        )
    }
    
    fun close() {
        httpClient.close()
    }
}

// Notion Service
class NotionService {
    private val httpClient = HttpClient(CIO)
    private val notionToken = "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"
    
    companion object {
        // Notion APIが利用できない場合のフォールバックストレージ
        private val localTextStorage = mutableMapOf<String, String>()
    }
    
    suspend fun createPage(title: String, content: String, parentPageId: String? = null): String {
        return try {
            // まずワークスペースのルートページに作成を試行
            val requestBody = buildJsonObject {
                put("parent", buildJsonObject {
                    put("type", JsonPrimitive("workspace"))
                    put("workspace", JsonPrimitive(true))
                })
                put("properties", buildJsonObject {
                    put("title", buildJsonObject {
                        put("title", buildJsonArray {
                            add(buildJsonObject {
                                put("text", buildJsonObject {
                                    put("content", JsonPrimitive(title))
                                })
                            })
                        })
                    })
                })
                put("children", buildJsonArray {
                    add(buildJsonObject {
                        put("object", JsonPrimitive("block"))
                        put("type", JsonPrimitive("paragraph"))
                        put("paragraph", buildJsonObject {
                            put("rich_text", buildJsonArray {
                                add(buildJsonObject {
                                    put("type", JsonPrimitive("text"))
                                    put("text", buildJsonObject {
                                        put("content", JsonPrimitive(content))
                                    })
                                })
                            })
                        })
                    })
                })
            }
            
            val response = httpClient.post("https://api.notion.com/v1/pages") {
                headers {
                    append("Authorization", "Bearer $notionToken")
                    append("Content-Type", "application/json")
                    append("Notion-Version", "2022-06-28")
                }
                setBody(requestBody.toString())
            }
            
            if (response.status.value in 200..299) {
                val responseBody = response.bodyAsText()
                val json = Json.parseToJsonElement(responseBody).jsonObject
                json["id"]?.jsonPrimitive?.content ?: "unknown-id"
            } else {
                val errorBody = response.bodyAsText()
                // ワークスペースルートで失敗した場合はシンプルなテキストファイルとして保存
                createSimpleTextContent(title, content)
            }
        } catch (e: Exception) {
            // Notion APIが失敗した場合はローカルストレージでシミュレート
            createSimpleTextContent(title, content)
        }
    }
    
    private fun createSimpleTextContent(title: String, content: String): String {
        // Notion APIが利用できない場合のフォールバック
        val pageId = "local-${System.currentTimeMillis()}"
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        
        // シンプルなテキストファイルとしてシミュレート
        val textContent = """
            タイトル: $title
            作成日時: $timestamp
            内容:
            $content
            
            (注意: Notion APIが利用できないため、ローカルストレージでシミュレートしました)
        """.trimIndent()
        
        // ローカルメモリに保存 (実際のアプリではファイルシステムやデータベースに保存)
        localTextStorage[pageId] = textContent
        
        return pageId
    }
    
    fun close() {
        httpClient.close()
    }
}

// Memory Service
@Serializable
data class MemoryData(
    val value: String,
    val description: String,
    val timestamp: Long
)

class MemoryService {
    private val storage = mutableMapOf<String, MemoryData>()
    
    suspend fun store(key: String, value: String, description: String = "") {
        storage[key] = MemoryData(
            value = value,
            description = description,
            timestamp = System.currentTimeMillis()
        )
    }
    
    suspend fun get(key: String): MemoryData? {
        return storage[key]
    }
    
    suspend fun list(): Map<String, MemoryData> {
        return storage.toMap()
    }
    
    suspend fun remove(key: String): Boolean {
        return storage.remove(key) != null
    }
    
    suspend fun clear() {
        storage.clear()
    }
}

// System Information Service
class SystemInfoService {
    
    fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
    
    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy年MM月dd日 (EEEE)", Locale.JAPANESE)
        return formatter.format(Date())
    }
    
    fun getDeviceInfo(): String {
        return buildString {
            append("デバイス: ${Build.MANUFACTURER} ${Build.MODEL}")
            append("\nAndroidバージョン: ${Build.VERSION.RELEASE}")
            append("\nAPIレベル: ${Build.VERSION.SDK_INT}")
            append("\nビルド: ${Build.DISPLAY}")
        }
    }
    
    fun getDetailedSystemInfo(): String {
        return buildString {
            append("=== システム情報 ===\n")
            append("デバイス製造元: ${Build.MANUFACTURER}\n")
            append("デバイスモデル: ${Build.MODEL}\n")
            append("デバイス名: ${Build.DEVICE}\n")
            append("プロダクト: ${Build.PRODUCT}\n")
            append("ボード: ${Build.BOARD}\n")
            append("ハードウェア: ${Build.HARDWARE}\n")
            append("ブランド: ${Build.BRAND}\n")
            append("タイプ: ${Build.TYPE}\n")
            append("タグ: ${Build.TAGS}\n")
            append("フィンガープリント: ${Build.FINGERPRINT}\n")
            append("\n=== Android情報 ===\n")
            append("Androidバージョン: ${Build.VERSION.RELEASE}\n")
            append("APIレベル: ${Build.VERSION.SDK_INT}\n")
            append("ビルドID: ${Build.ID}\n")
            append("ディスプレイ: ${Build.DISPLAY}\n")
            append("ホスト: ${Build.HOST}\n")
            append("ユーザー: ${Build.USER}\n")
            append("時間: ${Build.TIME}\n")
            append("\n=== Runtime情報 ===\n")
            append("利用可能プロセッサ数: ${Runtime.getRuntime().availableProcessors()}\n")
            append("最大メモリ: ${Runtime.getRuntime().maxMemory() / (1024 * 1024)} MB\n")
            append("合計メモリ: ${Runtime.getRuntime().totalMemory() / (1024 * 1024)} MB\n")
            append("空きメモリ: ${Runtime.getRuntime().freeMemory() / (1024 * 1024)} MB\n")
        }
    }
}
