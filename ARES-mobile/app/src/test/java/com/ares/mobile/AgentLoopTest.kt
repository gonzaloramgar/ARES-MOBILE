package com.ares.mobile

import com.ares.mobile.agent.AgentLoop
import com.ares.mobile.agent.ChatMessage
import com.ares.mobile.agent.ConversationHistory
import com.ares.mobile.agent.MessageRole
import com.ares.mobile.agent.ToolRegistry
import com.ares.mobile.ai.GemmaClient
import com.ares.mobile.ai.GemmaStreamEvent
import com.ares.mobile.ai.ModelManager
import com.ares.mobile.ai.ModelSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AgentLoopTest {
    @Test
    fun storesAssistantReplyWhenModelReturnsPlainText() = runTest {
        val history = MutableStateFlow(
            listOf(
                ChatMessage(
                    id = 1,
                    role = MessageRole.User,
                    content = "hola",
                    timestamp = 1L,
                )
            )
        )
        val conversationHistory = mockk<ConversationHistory>(relaxed = true)
        every { conversationHistory.messages } returns history
        coEvery { conversationHistory.addUserMessage(any()) } answers { }
        coEvery { conversationHistory.addAssistantMessage(any(), any()) } answers { }

        val toolRegistry = mockk<ToolRegistry>()
        every { toolRegistry.definitions() } returns emptyList()

        val gemmaClient = mockk<GemmaClient>()
        every {
            gemmaClient.streamReply(any(), any(), any())
        } returns flowOf(
            GemmaStreamEvent.Token("Hola"),
            GemmaStreamEvent.Completed("Hola"),
        )

        val modelManager = mockk<ModelManager>()
        every { modelManager.settingsFlow } returns flowOf(ModelSettings())

        val agentLoop = AgentLoop(conversationHistory, toolRegistry, gemmaClient, modelManager)

        val emissions = agentLoop.submitUserMessage("hola").toList()

        coVerify { conversationHistory.addUserMessage("hola") }
        coVerify { conversationHistory.addAssistantMessage("Hola", any()) }
        assertFalse(emissions.last().isGenerating)
        assertEquals("", emissions.last().draftResponse)
    }
}