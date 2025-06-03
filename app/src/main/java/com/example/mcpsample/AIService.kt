package com.example.mcpsample

enum class AIProvider {
    MOCK, OPENAI, GEMINI
}

class AIService {
    companion object {
        var currentProvider = AIProvider.GEMINI // デフォルトをGeminiに設定
    }
    
    private val openAiClient: OpenAIClient by lazy {
        OpenAIClient(BuildConfig.OPENAI_API_KEY)
    }
    
    private val geminiClient: GeminiClient by lazy {
        GeminiClient(BuildConfig.GEMINI_API_KEY)
    }
    
    // MCP統合用のクライアント
    private val mcpClientManager = McpClientManager()
    
    suspend fun sendChatMessage(prompt: String): String {
        return when (currentProvider) {
            AIProvider.MOCK -> generateMockResponse(prompt)
            AIProvider.OPENAI -> {
                try {
                    val enhancedPrompt = enhancePromptWithMcp(prompt)
                    openAiClient.chatCompletion(enhancedPrompt)
                } catch (e: Exception) {
                    "OpenAI Error: ${e.message}"
                }
            }
            AIProvider.GEMINI -> {
                try {
                    val enhancedPrompt = enhancePromptWithMcp(prompt)
                    geminiClient.generateContent(enhancedPrompt)
                } catch (e: Exception) {
                    "Gemini Error: ${e.message}"
                }
            }
        }
    }
    
    private suspend fun enhancePromptWithMcp(prompt: String): String {
        // MCPツールが使用可能な場合、コンテキストを追加
        val mcpContext = buildString {
            append("あなたはMCP (Model Context Protocol) に対応したAIアシスタントです。\n")
            append("以下のツールが利用可能です:\n")
            append("- Notion API: ページ作成、データベース操作\n")
            append("- Weather API: 天気情報取得\n")
            append("- File System: ファイル操作\n")
            append("- Memory: 情報の記憶と検索\n\n")
            append("ユーザーの質問: $prompt")
        }
        
        return mcpContext
    }
    
    private suspend fun generateMockResponse(prompt: String): String {
        return "This is a mock response for: $prompt\n\nTo enable real AI responses, switch to OpenAI or Gemini mode using the provider toggle button."
    }
    
    fun close() {
        openAiClient.close()
        geminiClient.close()
    }
    
    fun getProviderName(): String {
        return when (currentProvider) {
            AIProvider.MOCK -> "Mock AI"
            AIProvider.OPENAI -> "OpenAI GPT-4o-mini"
            AIProvider.GEMINI -> "Google Gemini"
        }
    }
}
