package com.ares.mobile.agent

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ToolResult(
    val success: Boolean,
    val content: String,
    val data: JsonObject? = null,
)