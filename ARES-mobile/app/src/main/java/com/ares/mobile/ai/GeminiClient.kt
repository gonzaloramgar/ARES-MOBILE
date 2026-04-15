package com.ares.mobile.ai

import android.util.Base64
import com.ares.mobile.agent.ChatMessage
import com.ares.mobile.agent.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * REST client for Google Gemini API.
 * Used as an online fallback when both internet and a Gemini API key are available.
 * Free tier: gemini-2.0-flash  →  https://ai.google.dev/
 */
object GeminiClient {

    private const val BASE = "https://generativelanguage.googleapis.com/v1beta/models"
    private const val MODEL = "gemini-2.0-flash"

    // ── Text generation ──────────────────────────────────────────────────

    suspend fun generate(
        history: List<ChatMessage>,
        systemPrompt: String,
        apiKey: String,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = openJson("$BASE/$MODEL:generateContent?key=$apiKey")
            val body = buildTextRequest(history, systemPrompt)
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) return@runCatching null
            parseResponse(conn.inputStream.use { it.readBytes().decodeToString() })
        }.getOrNull()
    }

    // ── Vision: analyze an image ────────────────────────────────────────

    suspend fun analyzeImage(
        imageBytes: ByteArray,
        prompt: String,
        apiKey: String,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = openJson("$BASE/$MODEL:generateContent?key=$apiKey")
            val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val body = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                        put(JSONObject().apply {
                            put("inlineData", JSONObject()
                                .put("mimeType", "image/jpeg")
                                .put("data", base64))
                        })
                    })
                ))
                put("generationConfig", JSONObject()
                    .put("temperature", 0.4)
                    .put("maxOutputTokens", 512))
            }.toString()
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) return@runCatching null
            parseResponse(conn.inputStream.use { it.readBytes().decodeToString() })
        }.getOrNull()
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun openJson(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
        }

    private fun buildTextRequest(history: List<ChatMessage>, systemPrompt: String): String {
        // Gemini requires strictly alternating user/model turns
        val turns = mergeConsecutiveRoles(
            history.filter { it.role != MessageRole.System },
        )

        val contents = JSONArray()
        for (msg in turns) {
            val role = if (msg.role == MessageRole.Assistant) "model" else "user"
            contents.put(
                JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(JSONObject().put("text", msg.content))),
            )
        }

        return JSONObject().apply {
            put("systemInstruction", JSONObject()
                .put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            put("contents", contents)
            put("generationConfig", JSONObject()
                .put("temperature", 0.7)
                .put("maxOutputTokens", 1024))
        }.toString()
    }

    /** Merge consecutive messages with the same effective role to satisfy Gemini's constraint. */
    private fun mergeConsecutiveRoles(msgs: List<ChatMessage>): List<ChatMessage> {
        if (msgs.isEmpty()) return msgs
        // Normalise: Tool → User
        val normalised = msgs.map { if (it.role == MessageRole.Tool) it.copy(role = MessageRole.User) else it }
        val result = mutableListOf(normalised[0])
        for (i in 1 until normalised.size) {
            val cur = normalised[i]
            val prev = result.last()
            if (cur.role == prev.role) {
                result[result.lastIndex] = prev.copy(content = "${prev.content}\n${cur.content}")
            } else {
                result.add(cur)
            }
        }
        return result
    }

    private fun parseResponse(json: String): String? = try {
        val parts = JSONObject(json)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
        buildString { repeat(parts.length()) { i -> append(parts.getJSONObject(i).optString("text")) } }
            .trim().takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}
