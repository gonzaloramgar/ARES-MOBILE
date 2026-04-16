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
    private val networkChecker: NetworkChecker,
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
                val toolName = lastMessage.toolName ?: "herramienta"
                val result = lastMessage.content
                val settings = modelManager.settingsFlow.firstOrNull()
                val geminiKey = settings?.geminiApiKey

                val summary = if (!geminiKey.isNullOrBlank() && networkChecker.isOnline()) {
                    // Let Gemini craft a natural-language follow-up to the tool result
                    GeminiClient.generate(
                        history = history.takeLast(6),
                        systemPrompt = buildSystemPrompt(tools),
                        apiKey = geminiKey,
                    ) ?: buildToolResultSummary(toolName, result)
                } else {
                    buildToolResultSummary(toolName, result)
                }
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

                val settings = modelManager.settingsFlow.firstOrNull()
                val geminiKey = settings?.geminiApiKey
                val hfToken = settings?.hfAccessToken
                val useGwopus = settings?.preference == ModelPreference.GWOPUS35

                val baseReply = if (useGwopus && !hfToken.isNullOrBlank() && networkChecker.isOnline()) {
                    // ── Online GWOPUS profile (Qwen on Hugging Face) ────
                    HuggingFaceClient.generate(
                        history = history,
                        systemPrompt = buildSystemPrompt(tools),
                        hfToken = hfToken,
                    ) ?: "No pude consultar GWOPUS 3.5 ahora mismo. Revisa token/red o vuelve a Gemma local."
                } else if (useGwopus && (hfToken.isNullOrBlank() || !networkChecker.isOnline())) {
                    if (hfToken.isNullOrBlank()) {
                        "Para usar GWOPUS 3.5 necesitas token de Hugging Face en Ajustes."
                    } else {
                        "GWOPUS 3.5 es online y ahora no tengo conexión. Cambia a Gemma local o activa internet."
                    }
                } else if (!geminiKey.isNullOrBlank() && networkChecker.isOnline()) {
                    // ── Online: use Gemini API ───────────────────────────
                    GeminiClient.generate(
                        history = history,
                        systemPrompt = buildSystemPrompt(tools),
                        apiKey = geminiKey,
                    ) ?: buildFallbackResponse(lastMessage.content, null)
                } else {
                    // ── Offline: use local LiteRT-LM model ───────────────
                    val installState = modelManager.observeInstallState().firstOrNull()
                    val prompt = buildPrompt(history, tools)
                    if (installState is ModelInstallState.Ready) {
                        tryGenerateWithLiteRt(prompt, File(installState.path))
                            ?: buildFallbackResponse(lastMessage.content, installState)
                    } else {
                        buildFallbackResponse(lastMessage.content, installState)
                    }
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
        val q = input.lowercase().trim()

        // — Clipboard —
        if (q.startsWith("/clipboard read") || (q.contains("portapapeles") && (q.contains("lee") || q.contains("leer") || q.contains("qué hay") || q.contains("que hay") || q.contains("muestra") || q.contains("ver")))) {
            return ("clipboard" to buildJsonObject { put("action", "read") }).takeIfAvailable(availableTools)
        }
        if (q.startsWith("/clipboard write ")) {
            return ("clipboard" to buildJsonObject {
                put("action", "write")
                put("text", input.removePrefix("/clipboard write ").trim())
            }).takeIfAvailable(availableTools)
        }
        if (q.contains("copia") && q.contains("portapapeles")) {
            val text = input.substringAfter("\"").substringBefore("\"").takeIf { it.isNotBlank() }
                ?: input.substringAfterLast(" ")
            return ("clipboard" to buildJsonObject {
                put("action", "write")
                put("text", text)
            }).takeIfAvailable(availableTools)
        }

        // — Location —
        val locationTriggers = listOf(
            "donde estoy", "dónde estoy", "/location",
            "mi ubicacion", "mi ubicación", "cual es mi ubicacion", "cuál es mi ubicación",
            "en que ciudad estoy", "en qué ciudad estoy",
            "en que lugar estoy", "en qué lugar estoy",
            "donde me encuentro", "dónde me encuentro",
            "localizacion", "localización", "gps", "mi posicion", "mi posición",
            "dime donde estoy", "dime dónde estoy",
        )
        if (locationTriggers.any { q.contains(it) }) {
            return ("location" to buildJsonObject { put("action", "current") }).takeIfAvailable(availableTools)
        }

        // — Alarm —
        if (q.startsWith("/alarm ")) {
            val rest = q.removePrefix("/alarm ").trim()
            val minutes = rest.substringBefore(' ').toLongOrNull() ?: 10L
            val title = rest.substringAfter(' ', "").trim()
                .takeIf { it.isNotBlank() } ?: "Recordatorio ARES"
            return ("alarm" to buildJsonObject {
                put("minutesFromNow", minutes)
                put("title", title.replaceFirstChar { it.uppercase() })
            }).takeIfAvailable(availableTools)
        }
        val alarmTriggers = listOf("recuerdame", "recuérdame", "pon una alarma", "ponme una alarma",
            "programa una alarma", "crea una alarma", "avisame", "avísame", "despiertame",
            "despiértame")
        if (alarmTriggers.any { q.startsWith(it) || q.contains(it) }) {
            val minutes = extractMinutes(q) ?: 10L
            val title = extractAlarmTitle(input, q) ?: "Recordatorio ARES"
            return ("alarm" to buildJsonObject {
                put("minutesFromNow", minutes)
                put("title", title)
            }).takeIfAvailable(availableTools)
        }

        // — Voice/TTS —
        if (q.startsWith("/speak ")) {
            return ("voice" to buildJsonObject {
                put("action", "speak")
                put("text", input.removePrefix("/speak ").trim())
            }).takeIfAvailable(availableTools)
        }
        if (q.startsWith("di ") || q.startsWith("lee en voz alta ") || q.startsWith("habla ")) {
            val text = input.substringAfter(" ").trim()
            return ("voice" to buildJsonObject {
                put("action", "speak")
                put("text", text)
            }).takeIfAvailable(availableTools)
        }

        // — Camera —
        if (q.startsWith("/camera") || q.contains("haz una foto") || q.contains("saca una foto") ||
            q.contains("toma una foto") || q.contains("captura") ||
            (q.contains("camara") || q.contains("cámara")) && (q.contains("abre") || q.contains("usa") || q.contains("foto"))
        ) {
            return ("camera" to buildJsonObject { put("action", "capture") }).takeIfAvailable(availableTools)
        }

        return null
    }

    private fun Pair<String, JsonObject>.takeIfAvailable(tools: Set<String>): Pair<String, JsonObject>? =
        takeIf { tools.contains(first) }

    private fun extractMinutes(text: String): Long? {
        // Numeric: "en 5 minutos", "a los 30 minutos"
        Regex("""(?:en|a los?|dentro de)\s+(\d+)\s+minuto""").find(text)?.let {
            return it.groupValues[1].toLongOrNull()
        }
        // Numeric without preposition: "5 minutos"
        Regex("""(\d+)\s+minuto""").find(text)?.let {
            return it.groupValues[1].toLongOrNull()
        }
        // Numeric hours: "en 1 hora", "en 2 horas"
        Regex("""(?:en|a la[s]?|dentro de)\s+(\d+)\s+hora""").find(text)?.let {
            return (it.groupValues[1].toLongOrNull() ?: 1L) * 60L
        }
        // Word numbers in Spanish
        val wordMap = mapOf(
            "un " to 1L, "una " to 1L, "dos " to 2L, "tres " to 3L, "cuatro " to 4L,
            "cinco " to 5L, "seis " to 6L, "siete " to 7L, "ocho " to 8L, "nueve " to 9L,
            "diez " to 10L, "quince " to 15L, "veinte " to 20L, "treinta " to 30L,
            "cuarenta " to 40L, "cincuenta " to 50L, "sesenta " to 60L,
        )
        for ((word, num) in wordMap) {
            if (text.contains(word)) return num
        }
        return null
    }

    private fun extractAlarmTitle(original: String, lower: String): String? {
        // "recuérdame en X minutos que [title]"
        val afterQue = Regex("""que\s+(.+)$""").find(lower)?.groupValues?.get(1)?.trim()
        if (!afterQue.isNullOrBlank() && afterQue.length > 3) {
            return afterQue.replaceFirstChar { it.uppercase() }
        }
        // After last time reference: try to get title from the original
        val afterMinutos = Regex("""minutos?\s+(.+)$""", RegexOption.IGNORE_CASE).find(original)?.groupValues?.get(1)?.trim()
        if (!afterMinutos.isNullOrBlank() && afterMinutos.length > 2) return afterMinutos
        return null
    }

    private fun buildSystemPrompt(tools: List<ToolDefinition> = emptyList()): String = buildString {
        append(
            """
            Eres ARES Mobile, asistente de IA en Android.
            Personalidad: directo, útil, conciso. Sin introducciones ni despedidas formales.
            Idioma: siempre en español, tuteo, tono natural como un colega técnico.
            Nunca digas "Como IA..." ni "No tengo emociones...". Eres ARES, punto.
            Respuestas cortas salvo que el usuario pida detalle.
            """.trimIndent(),
        )
        if (tools.isNotEmpty()) {
            append("\n\nHerramientas disponibles: ")
            append(tools.joinToString(", ") { "${it.name} — ${it.description}" })
        }
    }

    private fun buildPrompt(history: List<ChatMessage>, tools: List<ToolDefinition> = emptyList()): String = buildString {
        append("""
            Eres ARES Mobile, asistente de IA local ejecutandose directamente en Android.
            Personalidad: directo, util, conciso. Sin introducciones largas ni despedidas formales.
            Idioma: siempre en espanol, tuteo, tono natural como un colega tecnico.
            Nunca digas "Como IA..." ni "No tengo emociones...". Eres ARES, punto.
            Si te preguntan algo que requiere buscar en internet, dilo claramente y ofrece lo que sabes.
            Respuestas cortas salvo que el usuario pida detalle.
        """.trimIndent())

        if (tools.isNotEmpty()) {
            append("\n\nHerramientas disponibles (usa /toolname para invocarlas):\n")
            tools.forEach { tool ->
                append("- /${tool.name}: ${tool.description}\n")
            }
            append("Cuando el usuario pida algo que requiera una herramienta, invocala directamente.\n")
        }

        append("\n")
        history.takeLast(14).forEach { message ->
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
            val builder = optionsClass.getMethod("builder").invoke(null) ?: return@runCatching null

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

    private fun buildToolResultSummary(toolName: String, result: String): String = when {
        // Location
        toolName == "location" && result.startsWith("Permiso") ->
            "No tengo permiso de ubicación. $result"
        toolName == "location" && result.startsWith("No se pudo") ->
            "No pude obtener tu ubicación. $result"
        toolName == "location" ->
            "Estás en $result"

        // Alarm
        toolName == "alarm" ->
            "Hecho. $result."

        // Clipboard read
        toolName == "clipboard" && result == "El portapapeles está vacío" ->
            "El portapapeles está vacío."
        toolName == "clipboard" && result == "Texto copiado al portapapeles" ->
            "Listo, ya está en el portapapeles."
        toolName == "clipboard" ->
            "Portapapeles: \"$result\""

        // Voice
        toolName == "voice" ->
            "Dicho."

        // Camera
        toolName == "camera" ->
            "Foto hecha. $result"

        // Fallback
        result.startsWith("Error") || result.startsWith("No ") ->
            result
        else ->
            result
    }

    private fun buildFallbackReply(input: String): String {
        val q = input.trim().lowercase()
        return when {
            // Saludos
            q.matches(Regex("(hola|buenas|hey|ey|ola|buenos dias|buenas tardes|buenas noches).*")) ->
                "Listo. ¿Qué necesitas?"

            // Preguntas sobre identidad
            q.contains("quien eres") || q.contains("quién eres") || q.contains("que eres") || q.contains("qué eres") ->
                "Soy ARES Mobile, tu asistente local en Android. Corro completamente en tu dispositivo, sin internet. ¿En qué te ayudo?"

            // Capacidades
            q.contains("que puedes") || q.contains("qué puedes") || q.contains("para que sirves") || q.contains("para qué sirves") ->
                "Puedo chatear contigo, leer el portapapeles, saber tu ubicación, hacer fotos, poner alarmas y hablar en voz alta. Todo sin salir del dispositivo. Prueba escribiendo /location, /alarm, /clipboard o usa los botones del chat."

            // Herramientas
            q.contains("alarm") || q.contains("recordatorio") || q.contains("recordar") ->
                "Usa /alarm <minutos> <titulo> o dime cuándo quieres el recordatorio y lo programo."

            q.contains("ubicacion") || q.contains("ubicación") || q.contains("donde estoy") || q.contains("dónde estoy") ->
                "Un momento, uso /location para obtener tu posición."

            q.contains("portapapeles") || q.contains("clipboard") ->
                "Para leer el portapapeles di 'lee el portapapeles' o usa /clipboard read. Para escribir: /clipboard write <texto>."

            q.contains("camara") || q.contains("cámara") || q.contains("foto") ->
                "Pulsa el botón de cámara en el chat o escribe /camera para hacer una foto y analizarla."

            // Estado del modelo
            q.contains("modelo") && (q.contains("install") || q.contains("descarg") || q.contains("descargar")) ->
                "Ve a Configuración → Instalación y pulsa 'Descargar'. Necesitarás unos 1.5-3 GB libres según el modelo que elijas."

            q.contains("modelo") ->
                "Estoy usando el motor Gemma 4 de Google, ejecutándose localmente en tu móvil. Sin llamadas a la nube."

            // Agradecimiento
            q.matches(Regex("(gracias|muchas gracias|genial|perfecto|ok gracias|vale gracias).*")) ->
                "Cuando quieras."

            // Pregunta sobre memoria
            q.contains("memoria") || q.contains("recuerdo") || q.contains("guardar") ->
                "En la pestaña Memoria puedes añadir y gestionar lo que quieres que recuerde. ¿Quieres que guarde algo ahora?"

            // Preguntas matemáticas simples
            q.matches(Regex(".*\\d+\\s*[+\\-*/×÷]\\s*\\d+.*")) -> {
                try {
                    val expr = q.replace("×", "*").replace("÷", "/")
                    val result = evalSimpleMath(expr)
                    if (result != null) "= $result" else "El modelo local no está cargado todavía. Instálalo desde Configuración."
                } catch (_: Exception) {
                    "El modelo local no está cargado todavía. Instálalo desde Configuración."
                }
            }

            // Preguntas directas cortas
            q.endsWith("?") && q.length < 60 ->
                "El modelo IA aún no está cargado. Instálalo desde la pestaña Configuración → Instalación para respuestas completas. ¿Puedo ayudarte con algo que no requiera el modelo, como alarmas o ubicación?"

            else ->
                "El modelo IA local no está activo todavía. Para respuestas completas, instálalo desde Configuración. Mientras tanto puedo ayudarte con herramientas: ubicación, alarmas, portapapeles y cámara."
        }
    }

    private fun evalSimpleMath(expr: String): String? {
        val pattern = Regex("(\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(\\d+(?:\\.\\d+)?)")
        val match = pattern.find(expr) ?: return null
        val a = match.groupValues[1].toDouble()
        val op = match.groupValues[2]
        val b = match.groupValues[3].toDouble()
        val result = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else return null
            else -> return null
        }
        return if (result == result.toLong().toDouble()) result.toLong().toString() else "%.4f".format(result).trimEnd('0').trimEnd('.')
    }
}