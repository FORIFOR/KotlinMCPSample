package com.example.mcpsample

import java.text.SimpleDateFormat
import java.util.*

data class UiState(
    val isServerRunning: Boolean = false,
    val logs: List<String> = emptyList(),
    val aiProvider: AIProvider = AIProvider.GEMINI_FUNCTIONS,
    val connectionStatus: String = "未接続",
    val availableTools: List<String> = emptyList(),
    val lastResponse: String = "",
    val isProcessing: Boolean = false
)

// ログエントリ作成のヘルパー関数
fun createLogEntry(message: String): String {
    val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    return "[$timestamp] $message"
}

// AI Provider の表示名を取得
fun AIProvider.getDisplayName(): String {
    return when (this) {
        AIProvider.MOCK -> "Mock AI"
        AIProvider.OPENAI -> "OpenAI"
        AIProvider.GEMINI -> "Gemini"
        AIProvider.GEMINI_FUNCTIONS -> "Gemini + Tools"
    }
}

// AI Provider のアイコンを取得
fun AIProvider.getIcon(): String {
    return when (this) {
        AIProvider.MOCK -> "🤖"
        AIProvider.OPENAI -> "🧠"
        AIProvider.GEMINI -> "💎"
        AIProvider.GEMINI_FUNCTIONS -> "🛠️"
    }
}

// AI Provider の説明を取得
fun AIProvider.getDescription(): String {
    return when (this) {
        AIProvider.MOCK -> "テスト用のモックAI"
        AIProvider.OPENAI -> "OpenAI GPT-4o-mini"
        AIProvider.GEMINI -> "Google Gemini (基本)"
        AIProvider.GEMINI_FUNCTIONS -> "Google Gemini + Function Calling"
    }
}
