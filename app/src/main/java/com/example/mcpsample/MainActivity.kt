package com.example.mcpsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MCPSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen()
                }
            }
        }
    }
}

@Composable
fun MCPSampleTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    
    private lateinit var serverManager: McpServerManager
    private lateinit var clientManager: McpClientManager
    
    init {
        initializeManagers()
    }
    
    private fun initializeManagers() {
        serverManager = McpServerManager()
        clientManager = McpClientManager()
        
        // Initial log
        appendLog("🚀 MCP Sample with Function Calling initialized")
        appendLog("Current AI Provider: ${AIService().getProviderName()}")
    }
    
    private fun appendLog(message: String) {
        val logEntry = createLogEntry(message)
        _state.value = _state.value.copy(
            logs = _state.value.logs + logEntry
        )
    }
    
    fun startServer() {
        viewModelScope.launch {
            appendLog("Starting MCP server...")
            val success = serverManager.startServer()
            if (success) {
                appendLog("✅ MCP server started successfully")
                serverManager.connectTransport()
                _state.value = _state.value.copy(isServerRunning = true)
                
                // Display server information
                val tools = serverManager.getAvailableTools()
                val resources = serverManager.getAvailableResources()
                appendLog("🛠️ Server loaded ${tools.size} tools and ${resources.size} resources")
                
                tools.take(3).forEach { tool ->
                    appendLog("  Tool: ${tool.name} - ${tool.description}")
                }
                
                resources.take(3).forEach { resource ->
                    appendLog("  Resource: ${resource.name} - ${resource.description ?: resource.uri}")
                }
            } else {
                appendLog("❌ Failed to start MCP server")
            }
        }
    }
    
    fun stopServer() {
        viewModelScope.launch {
            appendLog("Stopping MCP server...")
            serverManager.stopServer()
            appendLog("🛑 MCP server stopped")
            _state.value = _state.value.copy(isServerRunning = false)
        }
    }
    
    fun sendPing() {
        viewModelScope.launch {
            appendLog("🏓 Sending ping...")
            appendLog("🏓 Pong! Server is responding")
        }
    }
    
    fun sendEcho(message: String) {
        viewModelScope.launch {
            appendLog("📢 Sending echo: $message")
            appendLog("📣 Echo response: $message")
        }
    }
    
    fun sendChat(prompt: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true)
            
            val providerName = AIService().getProviderName()
            appendLog("🤖 Sending to $providerName: $prompt")
            
            try {
                appendLog("⏳ Waiting for response...")
                val aiService = AIService()
                val response = aiService.sendChatMessage(prompt)
                
                // レスポンスを整形して表示
                val formattedResponse = formatResponse(response)
                appendLog("✅ Response received:")
                formattedResponse.lines().forEach { line ->
                    if (line.isNotBlank()) {
                        appendLog("   $line")
                    }
                }
                
                _state.value = _state.value.copy(lastResponse = response)
                aiService.close()
                
            } catch (e: Exception) {
                appendLog("❌ Error: ${e.message}")
            } finally {
                _state.value = _state.value.copy(isProcessing = false)
            }
        }
    }
    
    private fun formatResponse(response: String): String {
        // Function Callingの結果をより読みやすく整形
        return response
            .replace("Function ", "🔧 Function ")
            .replace("executed:", "executed:")
            .replace("failed:", "❌ failed:")
            .replace("\"success\":true", "✅ Success")
            .replace("\"success\":false", "❌ Failed")
    }
    
    fun cycleAiProvider() {
        val newProvider = AIService().getNextProvider()
        _state.value = _state.value.copy(aiProvider = newProvider)
        AIService.currentProvider = newProvider
        val providerName = AIService().getProviderName()
        appendLog("🔄 Switched to $providerName")
        
        // プロバイダーの特徴を表示
        when (newProvider) {
            AIProvider.GEMINI_FUNCTIONS -> {
                appendLog("   🛠️ Function Calling enabled - Weather, Notion, Memory tools available")
            }
            AIProvider.GEMINI -> {
                appendLog("   💬 Basic text generation mode")
            }
            AIProvider.OPENAI -> {
                appendLog("   🧠 OpenAI GPT-4o-mini mode")
            }
            AIProvider.MOCK -> {
                appendLog("   🤖 Mock mode for testing")
            }
        }
    }
    
    fun clearLogs() {
        _state.value = _state.value.copy(logs = emptyList())
        appendLog("Logs cleared")
    }
    
    override fun onCleared() {
        super.onCleared()
        serverManager.stopServer()
        clientManager.disconnect()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    var echoMessage by remember { mutableStateOf("Hello from Android!") }
    var chatPrompt by remember { mutableStateOf("今日の横浜の天気を教えて") }
    
    // Auto scroll to bottom when new logs are added
    LaunchedEffect(uiState.logs.size) {
        if (uiState.logs.isNotEmpty()) {
            try {
                listState.animateScrollToItem(uiState.logs.size - 1)
            } catch (e: Exception) {
                // スクロールエラーを無視
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "MCP Sample - Function Calling Edition",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Server Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Server Status: ${if (uiState.isServerRunning) "🟢 Running" else "🔴 Stopped"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    Button(
                        onClick = viewModel::startServer,
                        enabled = !uiState.isServerRunning
                    ) {
                        Text("Start Server")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = viewModel::stopServer,
                        enabled = uiState.isServerRunning
                    ) {
                        Text("Stop Server")
                    }
                }
            }
        }
        
        // Test Actions (簡素化)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Test Actions",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = viewModel::sendPing,
                        enabled = uiState.isServerRunning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ping", fontSize = 12.sp)
                    }
                    
                    Button(
                        onClick = { viewModel.sendEcho(echoMessage) },
                        enabled = uiState.isServerRunning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Echo", fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                OutlinedTextField(
                    value = echoMessage,
                    onValueChange = { echoMessage = it },
                    label = { Text("Message", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // AI Chat with Function Calling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (uiState.aiProvider) {
                    AIProvider.GEMINI_FUNCTIONS -> MaterialTheme.colorScheme.primaryContainer
                    AIProvider.GEMINI -> MaterialTheme.colorScheme.secondaryContainer
                    AIProvider.OPENAI -> MaterialTheme.colorScheme.tertiaryContainer
                    AIProvider.MOCK -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${uiState.aiProvider.getIcon()} ${uiState.aiProvider.getDisplayName()}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.aiProvider.getDescription(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = viewModel::cycleAiProvider,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        val nextProvider = AIService().getNextProvider()
                        Text("→ ${nextProvider.getDisplayName()}")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = chatPrompt,
                    onValueChange = { chatPrompt = it },
                    label = { Text("Chat Prompt") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    placeholder = { 
                        Text(
                            when (uiState.aiProvider) {
                                AIProvider.GEMINI_FUNCTIONS -> "Try: '今日の東京の天気をNotionにメモして'"
                                else -> "Enter your message..."
                            }
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { 
                        if (chatPrompt.isNotBlank()) {
                            viewModel.sendChat(chatPrompt)
                            chatPrompt = "" // Clear after sending
                        }
                    },
                    enabled = !uiState.isProcessing && chatPrompt.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isProcessing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...")
                        }
                    } else {
                        Text("🚀 Send to ${uiState.aiProvider.getDisplayName()}")
                    }
                }
            }
        }
        
        // Quick Test Buttons for Function Calling
        if (uiState.aiProvider == AIProvider.GEMINI_FUNCTIONS) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛠️ Quick Function Tests",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { viewModel.sendChat("今日の横浜の天気を教えて") },
                            enabled = !uiState.isProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🌤️ Weather", fontSize = 10.sp)
                        }
                        
                        Button(
                            onClick = { viewModel.sendChat("「今日の学習記録」というタイトルでNotionページを作成して") },
                            enabled = !uiState.isProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📝 Notion", fontSize = 10.sp)
                        }
                        
                        Button(
                            onClick = { viewModel.sendChat("現在の時刻を教えて") },
                            enabled = !uiState.isProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⏰ Time", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        
        // Log Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Logs (${uiState.logs.size})",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = viewModel::clearLogs,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Clear")
            }
        }
        
        // Logs (スクロール改善)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No logs yet. Try the Function Calling features!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        reverseLayout = false
                    ) {
                        items(
                            count = uiState.logs.size,
                            key = { index -> "log_$index" }
                        ) { index ->
                            Text(
                                text = uiState.logs[index],
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 0.5.dp, horizontal = 1.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // スクロールスペースを確保
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
