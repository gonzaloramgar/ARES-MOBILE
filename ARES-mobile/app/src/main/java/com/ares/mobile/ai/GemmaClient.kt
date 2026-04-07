package com.ares.mobile.ai

import android.content.Context
import com.ares.mobile.agent.ChatMessage
import com.ares.mobile.agent.MessageRole
import com.ares.mobile.agent.ToolDefinition
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface GemmaStreamEvent {
    data class Token(val value: String) : GemmaStreamEvent
    data class ToolCall(val toolName: String, val arguments: JsonObject) : GemmaStreamEvent
    data class Completed(val fullText: String) : GemmaStreamEvent
    data class Failure(val message: String) : GemmaStreamEvent
}

class GemmaClient(
    private val context: Context,
    private val modelManager: ModelManager,
) {
    fun streamReply(
        history: List<ChatMessage>,
        tools: List<ToolDefinition>,
        thinkingEnabled: Boolean,
    ): Flow<GemmaStreamEvent> = flow {
        val lastMessage = history.lastOrNull()
        val toolMap = tools.associateBy { it.name }

        when (lastMessage?.role) {
            MessageRole.Tool -> {
                val summary = "Listo, ya usé ${lastMessage.toolName ?: "la herramienta"}. Resultado: ${lastMessage.content}"
                emitText(summary)
                emit(GemmaStreamEvent.Completed(summary))
            }

            MessageRole.User -> {
                val toolCall = detectToolCall(lastMessage.content, toolMap.keys)
                if (toolCall != null) {
                    emit(GemmaStreamEvent.ToolCall(toolCall.first, toolCall.second))
                    emit(GemmaStreamEvent.Completed(""))
                    return@flow
                }

                val installState = modelManager.observeInstallState().firstOrNull()
                val prompt = buildPrompt(history)
                val baseReply = if (installState is ModelInstallState.Ready) {
                    tryGenerateWithLiteRt(prompt, File(installState.path)) ?: buildFallbackResponse(lastMessage.content, installState)
                } else {
                    buildFallbackResponse(lastMessage.content, installState)
                }
                emitText(baseReply)
                emit(GemmaStreamEvent.Completed(baseReply))
            }

            else -> {
                val reply = "Listo para empezar."
                emitText(reply)
                emit(GemmaStreamEvent.Completed(reply))
            }
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<GemmaStreamEvent>.emitText(text: String) {
        val chunks = text.split(" ")
        val builder = StringBuilder()
        chunks.forEachIndexed { index, token ->
            builder.append(token)
            if (index < chunks.lastIndex) {
                builder.append(' ')
            }
            emit(GemmaStreamEvent.Token(builder.toString()))
            delay(18)
        }
    }

    private fun detectToolCall(input: String, availableTools: Set<String>): Pair<String, JsonObject>? {
        val normalized = input.lowercase()
        return when {
            normalized.startsWith("/clipboard read") || normalized.contains("portapapeles") && normalized.contains("leer") -> {
                "clipboard" to buildJsonObject { put("action", "read") }
            }

            normalized.startsWith("/clipboard write ") -> {
                val payload = input.removePrefix("/clipboard write ").trim()
                "clipboard" to buildJsonObject {
                    put("action", "write")
                    put("text", payload)
                }
            }

            normalized.startsWith("/location") || normalized.contains("ubicacion") || normalized.contains("ubicación") -> {
                "location" to buildJsonObject { put("action", "current") }
            }

            normalized.startsWith("/speak ") -> {
                "voice" to buildJsonObject {
                    put("action", "speak")
                    put("text", input.removePrefix("/speak ").trim())
                }
            }

            normalized.startsWith("/alarm ") -> {
                val minutes = normalized.removePrefix("/alarm ").substringBefore(' ').toLongOrNull() ?: 10L
                val title = input.removePrefix("/alarm ").substringAfter(' ', "Recordatorio ARES")
                "alarm" to buildJsonObject {
                    put("minutesFromNow", minutes)
                    put("title", title)
                }
            }

            normalized.startsWith("/camera") || normalized.contains("camara") || normalized.contains("cámara") -> {
                "camera" to buildJsonObject { put("action", "capture") }
            }

            else -> null
        }?.takeIf { availableTools.contains(it.first) }
    }

    private fun buildPrompt(history: List<ChatMessage>): String = buildString {
        append("""
            Eres ARES Mobile, un asistente local en Android.
            Responde en espanol con tono cercano y claro.
            Da respuestas utiles, directas y faciles de entender.
            Evita sonar robotico o repetir muletillas tecnicas.
            Si falta contexto, haz una sola pregunta corta para aclarar.
        """.trimIndent())
        append("\n\n")
        history.takeLast(12).forEach { message ->
            val role = when (message.role) {
                MessageRole.User -> "user"
                MessageRole.Assistant -> "assistant"
                MessageRole.Tool -> "tool"
                MessageRole.System -> "system"
            }
            append("<$role> ${message.content}\n")
        }
        append("<assistant> ")
    }

    private fun buildFallbackResponse(
        input: String,
        installState: ModelInstallState?,
    ): String = buildString {
        if (installState is ModelInstallState.Missing) {
            append("El modelo aún no está instalado. ")
        }
        append(buildFallbackReply(input))
    }

    private suspend fun tryGenerateWithLiteRt(prompt: String, modelFile: File): String? = withContext(Dispatchers.IO) {
        runCatching {
            val llmInferenceClass = Class.forName("com.google.mediapipe.tasks.genai.llminference.LlmInference")
            val optionsClass = Class.forName("com.google.mediapipe.tasks.genai.llminference.LlmInference\$LlmInferenceOptions")
            val builder = optionsClass.getMethod("builder").invoke(null)

            builder.invokeIfPresent("setModelPath", String::class.java, modelFile.absolutePath)
            builder.invokeIfPresent("setMaxTokens", Int::class.javaPrimitiveType!!, 1024)
            builder.invokeIfPresent("setTopK", Int::class.javaPrimitiveType!!, 40)
            builder.invokeIfPresent("setTemperature", Float::class.javaPrimitiveType!!, 0.7f)
            builder.invokeIfPresent("setRandomSeed", Int::class.javaPrimitiveType!!, 42)

            val options = builder.javaClass.methods.firstOrNull { method ->
                method.name == "build" && method.parameterCount == 0
            }?.invoke(builder) ?: return@runCatching null

            val inference = llmInferenceClass.methods.firstOrNull { method ->
                method.name == "createFromOptions" && method.parameterCount == 2
            }?.invoke(null, context, options) ?: return@runCatching null

            try {
                val responseMethod = inference.javaClass.methods.firstOrNull { method ->
                    method.name == "generateResponse" && method.parameterCount == 1
                } ?: return@runCatching null

                responseMethod.invoke(inference, prompt)?.toString()
            } finally {
                inference.javaClass.methods.firstOrNull { method ->
                    method.name == "close" && method.parameterCount == 0
                }?.invoke(inference)
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun Any.invokeIfPresent(methodName: String, parameterType: Class<*>, argument: Any) {
        javaClass.methods.firstOrNull { method ->
            method.name == methodName && method.parameterTypes.size == 1 && method.parameterTypes[0] == parameterType
        }?.invoke(this, argument)
    }

    private fun buildFallbackReply(input: String): String = when {
        input.contains("hola", ignoreCase = true) || input.contains("buenas", ignoreCase = true) -> {
            "Hola, que tal. Estoy listo para ayudarte con lo que necesites en ARES."
        }

        input.contains("gracias", ignoreCase = true) -> {
            "De nada. Si quieres, seguimos con el siguiente paso."
        }

        input.contains("memoria", ignoreCase = true) -> {
            "Perfecto. Puedes guardar, editar o borrar recuerdos desde la pestaña Memoria. Dime y te guio rapido."
        }

        input.contains("modelo", ignoreCase = true) -> {
            "Claro. Puedo ayudarte a revisar descarga, token o rendimiento del modelo y dejarlo fino."
        }

        else -> {
            "Entendido: \"$input\". Si quieres, te doy una respuesta mas corta o mas detallada segun prefieras."
        }
    }
}