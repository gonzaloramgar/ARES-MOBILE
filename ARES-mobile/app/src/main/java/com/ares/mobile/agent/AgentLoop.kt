package com.ares.mobile.agent

import com.ares.mobile.ai.GemmaClient
import com.ares.mobile.ai.GemmaStreamEvent
import com.ares.mobile.ai.ModelManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject

data class AgentLoopSnapshot(
    val draftResponse: String = "",
    val isGenerating: Boolean = false,
    val activeTool: String? = null,
    val error: String? = null,
)

class AgentLoop(
    private val conversationHistory: ConversationHistory,
    private val toolRegistry: ToolRegistry,
    private val gemmaClient: GemmaClient,
    private val modelManager: ModelManager,
) {
    fun submitUserMessage(content: String): Flow<AgentLoopSnapshot> = flow {
        if (content.isBlank()) {
            emit(AgentLoopSnapshot(error = "Escríbeme algo y te respondo ahora mismo."))
            return@flow
        }

        conversationHistory.addUserMessage(content)
        emit(AgentLoopSnapshot(isGenerating = true))

        var cycle = 0
        while (cycle < 3) {
            cycle += 1
            val settings = modelManager.settingsFlow.first()
            val history = conversationHistory.messages.value
            var latestDraft = ""
            var pendingTool: Pair<String, JsonObject>? = null

            gemmaClient.streamReply(
                history = history,
                tools = toolRegistry.definitions(),
                thinkingEnabled = settings.thinkingEnabled,
            ).collect { event ->
                when (event) {
                    is GemmaStreamEvent.Token -> {
                        latestDraft = event.value
                        emit(
                            AgentLoopSnapshot(
                                draftResponse = latestDraft,
                                isGenerating = true,
                                activeTool = pendingTool?.first,
                            )
                        )
                    }

                    is GemmaStreamEvent.ToolCall -> {
                        pendingTool = event.toolName to event.arguments
                        emit(
                            AgentLoopSnapshot(
                                draftResponse = latestDraft,
                                isGenerating = true,
                                activeTool = event.toolName,
                            )
                        )
                    }

                    is GemmaStreamEvent.Completed -> Unit
                    is GemmaStreamEvent.Failure -> emit(AgentLoopSnapshot(error = event.message))
                }
            }

            val toolCall = pendingTool
            if (toolCall == null) {
                if (latestDraft.isNotBlank()) {
                    conversationHistory.addAssistantMessage(latestDraft)
                }
                emit(AgentLoopSnapshot(isGenerating = false, draftResponse = ""))
                return@flow
            }

            val toolResult = toolRegistry.execute(toolCall.first, toolCall.second)
            conversationHistory.addToolMessage(toolCall.first, toolResult.content)
            emit(
                AgentLoopSnapshot(
                    isGenerating = true,
                    activeTool = toolCall.first,
                )
            )
        }

        val fallback = "No he podido terminar eso en este intento. Si quieres, lo vuelvo a intentar de otra forma."
        conversationHistory.addAssistantMessage(fallback)
        emit(AgentLoopSnapshot(isGenerating = false, error = fallback))
    }
}