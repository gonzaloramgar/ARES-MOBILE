package com.ares.mobile.agent

import com.ares.mobile.data.ConversationDao
import com.ares.mobile.data.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class MessageRole {
    System,
    User,
    Assistant,
    Tool,
}

data class ChatMessage(
    val id: Long = 0,
    val role: MessageRole,
    val content: String,
    val timestamp: Long,
    val toolName: String? = null,
)

class ConversationHistory(
    private val conversationDao: ConversationDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        scope.launch {
            conversationDao.observeMessages().collectLatest { entities ->
                _messages.value = entities.map { entity ->
                    ChatMessage(
                        id = entity.id,
                        role = entity.role.toMessageRole(),
                        content = entity.content,
                        timestamp = entity.timestamp,
                        toolName = entity.toolName,
                    )
                }
            }
        }
    }

    suspend fun addUserMessage(content: String) {
        insert(MessageRole.User, content)
    }

    suspend fun addAssistantMessage(content: String, toolName: String? = null) {
        insert(MessageRole.Assistant, content, toolName)
    }

    suspend fun addToolMessage(toolName: String, content: String) {
        insert(MessageRole.Tool, content, toolName)
    }

    suspend fun clearConversation() {
        conversationDao.clearConversation()
    }

    private suspend fun insert(role: MessageRole, content: String, toolName: String? = null) {
        conversationDao.insert(
            MessageEntity(
                role = role.name,
                content = content,
                timestamp = System.currentTimeMillis(),
                toolName = toolName,
            )
        )
    }

    private fun String.toMessageRole(): MessageRole =
        runCatching { MessageRole.valueOf(this) }.getOrDefault(MessageRole.Assistant)
}