package com.ares.mobile.ai

import com.ares.mobile.agent.ChatMessage
import com.ares.mobile.agent.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal client for Hugging Face Inference API.
 * Used for GWOPUS 3.5 mode (Qwen-based online model profile).
 */
object HuggingFaceClient {

    private const val MODEL_ID = "Qwen/Qwen2.5-1.5B-Instruct"
    private const val BASE = "https://api-inference.huggingface.co/models"

    suspend fun generate(
        history: List<ChatMessage>,
        systemPrompt: String,
        hfToken: String,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val prompt = buildPrompt(history, systemPrompt)
            val conn = openJson("$BASE/$MODEL_ID", hfToken)
            val body = JSONObject().apply {
                put("inputs", prompt)
                put("parameters", JSONObject().apply {
                    put("max_new_tokens", 384)
                    put("temperature", 0.65)
                    put("top_p", 0.9)
                    put("return_full_text", false)
                })
                put("options", JSONObject().put("wait_for_model", true))
            }.toString()

            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) return@runCatching null

            val raw = conn.inputStream.use { it.readBytes().decodeToString() }
            parseResponse(raw)
        }.getOrNull()
    }

    private fun openJson(url: String, hfToken: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $hfToken")
            setRequestProperty("User-Agent", "ARES-Mobile/1.0")
            doOutput = true
            connectTimeout = 20_000
            readTimeout = 60_000
        }

    private fun parseResponse(json: String): String? {
        val trimmed = json.trim()
        if (trimmed.startsWith("{")) {
            val obj = JSONObject(trimmed)
            if (obj.has("error")) return null
            val single = obj.optString("generated_text", "").trim()
            return single.takeIf { it.isNotBlank() }
        }

        val arr = JSONArray(trimmed)
        if (arr.length() == 0) return null
        val item = arr.getJSONObject(0)
        val text = item.optString("generated_text", "").trim()
        return text.takeIf { it.isNotBlank() }
    }

    private fun buildPrompt(history: List<ChatMessage>, systemPrompt: String): String {
        val sb = StringBuilder()
        sb.append("<|system|>\n")
        sb.append(systemPrompt.trim())
        sb.append("\n")

        history.takeLast(12).forEach { message ->
            val role = when (message.role) {
                MessageRole.Assistant -> "assistant"
                MessageRole.System -> "system"
                else -> "user"
            }
            sb.append("<|$role|>\n")
            sb.append(message.content.trim())
            sb.append("\n")
        }

        sb.append("<|assistant|>\n")
        return sb.toString()
    }
}
