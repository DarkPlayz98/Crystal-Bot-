package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class OllamaService(
    private val baseUrl: String,
    private val model: String,
    private val client: OkHttpClient = defaultClient
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun streamGenerate(
        conversation: List<GeminiMessage>,
        systemInstruction: String?,
        temperature: Float,
        onChunk: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = JSONArray()
            if (!systemInstruction.isNullOrBlank()) {
                messages.put(JSONObject().put("role", "system").put("content", systemInstruction))
            }
            conversation.forEach { message ->
                val messageJson = JSONObject()
                    .put("role", if (message.role == "model") "assistant" else message.role)
                    .put("content", message.parts.mapNotNull { it.text }.joinToString("\n"))
                val images = message.parts.mapNotNull { it.bitmap }.takeIf { it.isNotEmpty() }
                if (images != null) {
                    val imageArray = JSONArray()
                    images.forEach { imageArray.put(it.toBase64()) }
                    messageJson.put("images", imageArray)
                }
                messages.put(messageJson)
            }

            val payload = JSONObject()
                .put("model", model)
                .put("messages", messages)
                .put("stream", true)
                .put("options", JSONObject().put("temperature", temperature))
            val request = Request.Builder()
                .url(chatUrl())
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return@withContext Result.failure(
                        IllegalStateException(ollamaErrorMessage(body, response.code))
                    )
                }
                val body = response.body
                    ?: return@withContext Result.failure(IllegalStateException("Ollama returned an empty response."))
                val fullText = StringBuilder()
                BufferedReader(InputStreamReader(body.byteStream())).use { reader ->
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.isBlank()) continue
                        val chunk = JSONObject(line)
                        val error = chunk.optString("error")
                        if (error.isNotBlank()) {
                            return@withContext Result.failure(IllegalStateException(error))
                        }
                        val text = chunk.optJSONObject("message")?.optString("content").orEmpty()
                        if (text.isNotEmpty()) {
                            fullText.append(text)
                            onChunk(text)
                        }
                    }
                }
                if (fullText.isBlank()) {
                    Result.failure(IllegalStateException("Ollama returned no response text."))
                } else {
                    Result.success(fullText.toString())
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateSwarmAgentResponse(
        agentRole: String,
        agentName: String,
        swarmGoal: String,
        contextHistory: String
    ): Result<String> = streamGenerate(
        conversation = listOf(
            GeminiMessage(
                role = "user",
                parts = listOf(
                    GeminiMessagePart(
                        text = "You are $agentName, a specialist agent. Role: $agentRole\n" +
                            "Overall objective: $swarmGoal\nContext: $contextHistory\n" +
                            "Complete your specialist sub-task with a concise, actionable response."
                    )
                )
            )
        ),
        systemInstruction = null,
        temperature = 0.7f,
        onChunk = {}
    )

    private fun chatUrl(): HttpUrl {
        val base = baseUrl.trim().toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Enter a valid Ollama URL, such as http://10.0.2.2:11434.")
        require(base.scheme == "http" || base.scheme == "https") {
            "The Ollama URL must use HTTP or HTTPS."
        }
        require(base.username.isEmpty() && base.password.isEmpty() && base.query == null && base.fragment == null) {
            "The Ollama URL must not include credentials, a query, or a fragment."
        }
        return base.newBuilder().addPathSegments("api/chat").build()
    }

    private fun ollamaErrorMessage(body: String, statusCode: Int): String =
        try {
            JSONObject(body).optString("error").ifBlank { "Ollama request failed (HTTP $statusCode)." }
        } catch (_: Exception) {
            "Ollama request failed (HTTP $statusCode)."
        }

    private fun Bitmap.toBase64(): String {
        val output = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 85, output)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
