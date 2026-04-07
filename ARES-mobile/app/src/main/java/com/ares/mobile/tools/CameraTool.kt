package com.ares.mobile.tools

import android.content.Context
import com.ares.mobile.agent.ITool
import com.ares.mobile.agent.ToolDefinition
import com.ares.mobile.agent.ToolResult
import java.io.File
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CameraTool(
    private val context: Context,
) : ITool {
    override val definition: ToolDefinition = ToolDefinition(
        name = "camera",
        description = "Consume la última captura disponible de la app o solicita una nueva desde UI.",
        parameters = buildJsonObject {
            put("action", "capture|latest")
        },
    )

    override suspend fun execute(arguments: JsonObject): ToolResult {
        val lastCapture = File(context.filesDir, "captures/latest.jpg")
        return if (lastCapture.exists()) {
            ToolResult(true, "Última captura disponible en ${lastCapture.absolutePath}")
        } else {
            ToolResult(false, "No hay captura disponible todavía. Usa la acción de cámara desde la UI para crear una.")
        }
    }
}