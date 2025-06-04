package com.example.mcpsample

enum class AIProvider {
    MOCK, OPENAI, GEMINI, GEMINI_FUNCTIONS
}

class AIService {
    companion object {
        var currentProvider = AIProvider.GEMINI_FUNCTIONS // Function Calling対応Geminiをデフォルトに
    }
    
    private val openAiClient: OpenAIClient by lazy {
        OpenAIClient(BuildConfig.OPENAI_API_KEY)
    }
    
    private val geminiClient: GeminiClient by lazy {
        GeminiClient(BuildConfig.GEMINI_API_KEY)
    }
    
    private val geminiFunctionClient: GeminiClientWithFunctions by lazy {
        GeminiClientWithFunctions(BuildConfig.GEMINI_API_KEY)
    }
    
    suspend fun sendChatMessage(prompt: String): String {
        return when (currentProvider) {
            AIProvider.MOCK -> generateMockResponse(prompt)
            AIProvider.OPENAI -> {
                try {
                    openAiClient.chatCompletion(prompt)
                } catch (e: Exception) {
                    "OpenAI Error: ${e.message}"
                }
            }
            AIProvider.GEMINI -> {
                try {
                    geminiClient.generateContent(prompt)
                } catch (e: Exception) {
                    "Gemini Error: ${e.message}"
                }
            }
            AIProvider.GEMINI_FUNCTIONS -> {
                try {
                    // 🛠️ Function Calling対応Geminiを使用
                    geminiFunctionClient.generateContentWithTools(prompt)
                } catch (e: Exception) {
                    "Gemini Function Calling Error: ${e.message}"
                }
            }
        }
    }
    
    private suspend fun generateMockResponse(prompt: String): String {
        return buildString {
            append("🤖 Mock AI Response for: \"$prompt\"\n\n")
            
            when {
                prompt.contains("天気") -> {
                    append("天気情報のモック:\n")
                    append("📍 横浜の天気: 晴れ\n")
                    append("🌡️ 気温: 22°C\n")
                    append("💧 湿度: 65%\n")
                    append("💨 風速: 3 m/s")
                }
                prompt.contains("Notion") -> {
                    append("Notionページ作成のモック:\n")
                    append("📝 ページ「今日の学習記録」を作成しました\n")
                    append("🆔 ページID: mock-page-123\n")
                    append("✅ 作成完了")
                }
                prompt.contains("時刻") || prompt.contains("時間") -> {
                    append("時刻情報のモック:\n")
                    append("⏰ 現在時刻: 14:30:00\n")
                    append("📅 今日の日付: 2025年6月4日 (水曜日)")
                }
                else -> {
                    append("一般的なモック応答:\n")
                    append("実際のAI機能を使用するには、プロバイダーを切り替えてください。\n")
                    append("利用可能: OpenAI、Gemini、Gemini + Function Calling")
                }
            }
        }
    }
    
    fun close() {
        openAiClient.close()
        geminiClient.close()
        geminiFunctionClient.close()
    }
    
    fun getProviderName(): String {
        return when (currentProvider) {
            AIProvider.MOCK -> "Mock AI"
            AIProvider.OPENAI -> "OpenAI GPT-4o-mini"
            AIProvider.GEMINI -> "Google Gemini"
            AIProvider.GEMINI_FUNCTIONS -> "Gemini with Function Calling"
        }
    }
    
    fun getAvailableProviders(): List<AIProvider> {
        return AIProvider.values().toList()
    }
    
    fun setProvider(provider: AIProvider) {
        currentProvider = provider
    }
    
    fun getNextProvider(): AIProvider {
        return when (currentProvider) {
            AIProvider.GEMINI_FUNCTIONS -> AIProvider.GEMINI
            AIProvider.GEMINI -> AIProvider.OPENAI
            AIProvider.OPENAI -> AIProvider.MOCK
            AIProvider.MOCK -> AIProvider.GEMINI_FUNCTIONS
        }
    }
}
