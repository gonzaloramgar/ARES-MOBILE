package com.ares.mobile.agent

import kotlinx.serialization.json.JsonObject

interface ITool {
    val definition: ToolDefinition
    suspend fun execute(arguments: JsonObject): ToolResult
}