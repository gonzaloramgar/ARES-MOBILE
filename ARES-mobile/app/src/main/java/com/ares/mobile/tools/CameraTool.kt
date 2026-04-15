package com.ares.mobile.tools

import com.ares.mobile.agent.ITool
import com.ares.mobile.agent.ToolDefinition
import com.ares.mobile.agent.ToolResult
import com.ares.mobile.ai.GeminiClient
import com.ares.mobile.ai.ModelManager
import com.ares.mobile.ai.NetworkChecker
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

class CameraTool(
    private val filesDir: File,
    private val modelManager: ModelManager,
    private val networkChecker: NetworkChecker,
) : ITool {

    override val definition: ToolDefinition = ToolDefinition(
        name = "camera",
        description = "Analiza la última foto tomada desde la app. Describe objetos, texto, escena y cualquier información relevante.",
        parameters = buildJsonObject { put("action", "capture|latest") },
    )

    override suspend fun execute(arguments: JsonObject): ToolResult {
        val captureFile = File(filesDir, "captures/latest.jpg")

        if (!captureFile.exists()) {
            return ToolResult(
                success = false,
                content = "No hay ninguna foto disponible. Pulsa el botón de cámara en el chat para tomar una.",
            )
        }

        val settings = modelManager.settingsFlow.firstOrNull()
        val geminiKey = settings?.geminiApiKey

        return if (!geminiKey.isNullOrBlank() && networkChecker.isOnline()) {
            // ── Online: Gemini vision analysis ───────────────────────────
            val imageBytes = runCatching { captureFile.readBytes() }.getOrNull()
                ?: return ToolResult(false, "No se pudo leer la imagen del almacenamiento.")

            val description = GeminiClient.analyzeImage(
                imageBytes = imageBytes,
                prompt = """
                    Describe detalladamente en español qué ves en esta imagen.
                    Incluye: objetos presentes, textos visibles, escena o contexto, colores relevantes,
                    y cualquier información útil que el usuario pueda querer saber.
                    Sé conciso pero completo. Habla directamente, sin decir "En la imagen veo..." ni presentaciones.
                """.trimIndent(),
                apiKey = geminiKey,
            )

            if (description != null) {
                ToolResult(success = true, content = description)
            } else {
                ToolResult(
                    success = false,
                    content = "No pude analizar la imagen. Comprueba tu conexión o la clave de API de Gemini en Ajustes.",
                )
            }
        } else {
            // ── Offline: report why analysis is not available ─────────────
            val reason = when {
                !networkChecker.isOnline() -> "sin conexión a internet"
                geminiKey.isNullOrBlank() -> "sin clave API de Gemini configurada"
                else -> "no disponible"
            }
            ToolResult(
                success = true,
                content = "Foto guardada ($reason). Configura la API de Gemini en Ajustes → para análisis visual con IA.",
            )
        }
    }
}
