package com.ares.mobile.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.ares.mobile.agent.ITool
import com.ares.mobile.agent.ToolDefinition
import com.ares.mobile.agent.ToolResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class ClipboardTool(
    private val context: Context,
) : ITool {
    override val definition: ToolDefinition = ToolDefinition(
        name = "clipboard",
        description = "Lee o escribe el portapapeles del dispositivo.",
        parameters = buildJsonObject {
            put("action", "read|write")
            put("text", "string?")
        },
    )

    override suspend fun execute(arguments: JsonObject): ToolResult {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val action = arguments["action"]?.jsonPrimitive?.content ?: "read"
        return when (action) {
            "write" -> {
                val text = arguments["text"]?.jsonPrimitive?.content.orEmpty()
                clipboard.setPrimaryClip(ClipData.newPlainText("ARES", text))
                ToolResult(true, "Texto copiado al portapapeles")
            }

            else -> {
                val text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                ToolResult(true, if (text.isBlank()) "El portapapeles está vacío" else text)
            }
        }
    }
}