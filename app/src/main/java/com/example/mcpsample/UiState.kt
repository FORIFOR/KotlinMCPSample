package com.example.mcpsample

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.emptyList

data class UiState(
    val isServerRunning: Boolean = false,
    val logs: List<String> = emptyList(),
    val aiProvider: AIProvider = AIProvider.GEMINI
)

fun createLogEntry(message: String): String {
    val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
    return "[$timestamp] $message"
}
