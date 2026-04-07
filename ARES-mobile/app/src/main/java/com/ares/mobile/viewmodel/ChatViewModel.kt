package com.ares.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ares.mobile.agent.AgentLoop
import com.ares.mobile.agent.AgentLoopSnapshot
import com.ares.mobile.agent.ChatMessage
import com.ares.mobile.agent.ConversationHistory
import com.ares.mobile.ai.ModelInstallState
import com.ares.mobile.ai.ModelManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuickActionItem(
    val label: String,
    val prompt: String,
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val draftResponse: String = "",
    val isGenerating: Boolean = false,
    val activeTool: String? = null,
    val input: String = "",
    val modelHeadline: String = "Sin modelo",
    val installHint: String = "",
    val quickActions: List<QuickActionItem> = defaultQuickActions,
)

private val defaultQuickActions = listOf(
    QuickActionItem("Portapapeles", "/clipboard read"),
    QuickActionItem("Ubicación", "/location"),
    QuickActionItem("Leer en voz", "/speak Hola desde ARES móvil"),
    QuickActionItem("Alarma 10m", "/alarm 10 Recordatorio ARES"),
    QuickActionItem("Cámara", "/camera"),
)

class ChatViewModel(
    private val conversationHistory: ConversationHistory,
    private val agentLoop: AgentLoop,
    private val modelManager: ModelManager,
) : ViewModel() {
    private val input = MutableStateFlow("")
    private val loopSnapshot = MutableStateFlow(AgentLoopSnapshot())

    val uiState: StateFlow<ChatUiState> = combine(
        conversationHistory.messages,
        input,
        loopSnapshot,
        modelManager.settingsFlow,
        modelManager.observeInstallState(),
    ) { messages, currentInput, snapshot, _, installState ->
        ChatUiState(
            messages = messages,
            draftResponse = snapshot.draftResponse,
            isGenerating = snapshot.isGenerating,
            activeTool = snapshot.activeTool,
            input = currentInput,
            modelHeadline = buildModelHeadline(installState),
            installHint = buildInstallHint(installState),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatUiState(),
    )

    fun onInputChanged(value: String) {
        input.value = value
    }

    fun sendMessage() {
        val content = input.value.trim()
        submitMessage(content)
    }

    fun submitMessage(content: String) {
        if (content.isBlank()) return
        input.value = ""
        viewModelScope.launch {
            agentLoop.submitUserMessage(content).collect { snapshot ->
                loopSnapshot.value = snapshot
            }
        }
    }

    fun runQuickAction(prompt: String) {
        input.value = prompt
        sendMessage()
    }

    fun clearConversation() {
        viewModelScope.launch {
            conversationHistory.clearConversation()
            loopSnapshot.value = AgentLoopSnapshot()
        }
    }

    private fun buildModelHeadline(installState: ModelInstallState): String {
        return when (installState) {
            is ModelInstallState.Ready -> installState.variant.displayName
            is ModelInstallState.Missing -> "${installState.variant.displayName} • pendiente"
            is ModelInstallState.Downloading -> "${installState.variant.displayName} • ${installState.progressPercent}%"
            is ModelInstallState.Error -> "modelo con error"
            ModelInstallState.Checking -> "verificando modelo"
        }
    }

    private fun buildInstallHint(installState: ModelInstallState): String = when (installState) {
        is ModelInstallState.Missing -> "Modelo pendiente. Ábrelo en Ajustes para descargarlo."
        is ModelInstallState.Ready -> ""
        is ModelInstallState.Downloading -> "Descargando ${installState.variant.displayName}: ${installState.progressPercent}%"
        is ModelInstallState.Error -> "No pude preparar el modelo: ${installState.message}"
        ModelInstallState.Checking -> ""
    }
}