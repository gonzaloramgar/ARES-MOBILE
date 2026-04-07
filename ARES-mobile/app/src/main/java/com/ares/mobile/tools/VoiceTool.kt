package com.ares.mobile.tools

import android.content.Context
import android.speech.tts.TextToSpeech
import com.ares.mobile.agent.ITool
import com.ares.mobile.agent.ToolDefinition
import com.ares.mobile.agent.ToolResult
import java.util.Locale
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class VoiceTool(
    private val context: Context,
) : ITool {
    private var textToSpeech: TextToSpeech? = null

    override val definition: ToolDefinition = ToolDefinition(
        name = "voice",
        description = "Lee respuestas en voz alta y puede detener la síntesis.",
        parameters = buildJsonObject {
            put("action", "speak|stop")
            put("text", "string?")
        },
    )

    override suspend fun execute(arguments: JsonObject): ToolResult {
        val action = arguments["action"]?.jsonPrimitive?.content ?: "speak"
        return when (action) {
            "stop" -> {
                textToSpeech?.stop()
                ToolResult(true, "Síntesis de voz detenida")
            }

            else -> {
                val text = arguments["text"]?.jsonPrimitive?.content.orEmpty()
                ensureTts()
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ares-tts")
                ToolResult(true, if (text.isBlank()) "No se proporcionó texto para hablar" else "Leyendo en voz alta")
            }
        }
    }

    private fun ensureTts() {
        if (textToSpeech != null) return
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
            }
        }
    }
}