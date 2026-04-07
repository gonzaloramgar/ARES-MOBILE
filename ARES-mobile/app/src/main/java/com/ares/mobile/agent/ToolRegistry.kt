package com.ares.mobile.agent

import kotlinx.serialization.json.JsonObject

class ToolRegistry(
    tools: List<@JvmSuppressWildcards ITool>,
) {
    private val toolMap = tools.associateBy { it.definition.name }

    fun definitions(): List<ToolDefinition> = toolMap.values.map { it.definition }

    fun toolNames(): List<String> = toolMap.keys.sorted()

    suspend fun execute(toolName: String, arguments: JsonObject): ToolResult {
        val tool = toolMap[toolName]
            ?: return ToolResult(success = false, content = "Tool $toolName no registrada")
        return tool.execute(arguments)
    }
}