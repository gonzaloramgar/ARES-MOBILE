package com.ares.mobile

import com.ares.mobile.agent.ITool
import com.ares.mobile.agent.ToolDefinition
import com.ares.mobile.agent.ToolRegistry
import com.ares.mobile.agent.ToolResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolRegistryTest {
    private val fakeTool = object : ITool {
        override val definition: ToolDefinition = ToolDefinition(
            name = "echo",
            description = "Echo test tool",
            parameters = buildJsonObject { put("input", "string") },
        )

        override suspend fun execute(arguments: JsonObject): ToolResult =
            ToolResult(success = true, content = arguments.toString())
    }

    @Test
    fun registeredToolIsCallableByName() = runTest {
        val registry = ToolRegistry(listOf(fakeTool))
        val arguments = buildJsonObject { put("input", "hola") }

        val result = registry.execute("echo", arguments)

        assertTrue(result.success)
        assertTrue(result.content.contains("hola"))
    }

    @Test
    fun unknownToolReturnsFailure() = runTest {
        val registry = ToolRegistry(emptyList())

        val result = registry.execute("missing", buildJsonObject { })

        assertFalse(result.success)
    }

    @Test
    fun definitionsExposeRegisteredTools() {
        val registry = ToolRegistry(listOf(fakeTool))

        val definitions = registry.definitions()

        assertEquals(1, definitions.size)
        assertEquals("echo", definitions.first().name)
    }
}