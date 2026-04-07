package com.ares.mobile.agent

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)