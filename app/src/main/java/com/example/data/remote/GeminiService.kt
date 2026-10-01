package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

data class GeminiMessagePart(
    val text: String? = null,
    val bitmap: Bitmap? = null,
    val mimeType: String = "image/jpeg"
)

data class GeminiMessage(
    val role: String, // "user" or "model"
    val parts: List<GeminiMessagePart>
)

class GeminiService(
    private val customApiKey: String? = null
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getResolvedApiKey(): String {
        if (!customApiKey.isNullOrBlank()) return customApiKey
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun streamGenerate(
        conversation: List<GeminiMessage>,
        systemInstruction: String?,
        model: String = "gemini-3.5-flash",
        temperature: Float = 0.7f,
        onChunk: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getResolvedApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("GEMINI_API_KEY is not configured. Please add your key in AI Studio Secrets panel or Settings.")
            )
        }

        try {
            val requestJson = JSONObject()

            // Contents
            val contentsArray = JSONArray()
            for (msg in conversation) {
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.role == "assistant") "model" else msg.role)
                val partsArray = JSONArray()
                for (part in msg.parts) {
                    val partObj = JSONObject()
                    if (part.text != null) {
                        partObj.put("text", part.text)
                    }
                    if (part.bitmap != null) {
                        val inlineObj = JSONObject()
                        inlineObj.put("mimeType", part.mimeType)
                        inlineObj.put("data", part.bitmap.toBase64())
                        partObj.put("inlineData", inlineObj)
                    }
                    partsArray.put(partObj)
                }
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }
            requestJson.put("contents", contentsArray)

            // System Instruction
            if (!systemInstruction.isNullOrBlank()) {
                val sysObj = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                sysObj.put("parts", sysParts)
                requestJson.put("systemInstruction", sysObj)
            }

            // Generation Config
            val configObj = JSONObject()
            configObj.put("temperature", temperature)
            requestJson.put("generationConfig", configObj)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "HTTP error ${response.code}"
                val errorMsg = try {
                    JSONObject(errorBody).getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    errorBody
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
            val fullTextBuilder = StringBuilder()

            BufferedReader(InputStreamReader(body.byteStream())).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val rawLine = line?.trim() ?: continue
                    if (!rawLine.startsWith("data:")) continue
                    val jsonPayload = rawLine.removePrefix("data:").trim()
                    if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue

                    try {
                        val root = JSONObject(jsonPayload)
                        val candidates = root.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val textChunk = parts.getJSONObject(0).optString("text", "")
                                if (textChunk.isNotEmpty()) {
                                    fullTextBuilder.append(textChunk)
                                    withContext(Dispatchers.Main) {
                                        onChunk(textChunk)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // ignore chunk parse warning
                    }
                }
            }

            Result.success(fullTextBuilder.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateImage(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getResolvedApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("GEMINI_API_KEY is not configured.")
            )
        }

        try {
            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            val configObj = JSONObject()
            val imageConfig = JSONObject()
            imageConfig.put("aspectRatio", "1:1")
            imageConfig.put("imageSize", "1K")
            configObj.put("imageConfig", imageConfig)
            val modalities = JSONArray()
            modalities.put("TEXT")
            modalities.put("IMAGE")
            configObj.put("responseModalities", modalities)
            requestJson.put("generationConfig", configObj)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: "HTTP ${response.code}"
                return@withContext Result.failure(Exception("Image generation error: $err"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
            val root = JSONObject(body)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val inline = part.optJSONObject("inlineData")
                        if (inline != null) {
                            val data = inline.optString("data")
                            val mime = inline.optString("mimeType", "image/png")
                            return@withContext Result.success("data:$mime;base64,$data")
                        }
                    }
                }
            }

            Result.failure(Exception("No image returned by model"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}
