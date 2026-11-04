package com.example.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OllamaServiceTest {
    @Test
    fun `streams Ollama response and sends chat settings`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(
            MockResponse().setBody(
                """{"message":{"content":"Hello"},"done":false}""" + "\n" +
                    """{"message":{"content":" there"},"done":true}""" + "\n"
            )
        )
        server.start()
        try {
            val chunks = mutableListOf<String>()
            val result = OllamaService(server.url("/").toString(), "llama3.2")
                .streamGenerate(
                    conversation = listOf(
                        GeminiMessage("user", listOf(GeminiMessagePart(text = "Hi"))),
                        GeminiMessage("model", listOf(GeminiMessagePart(text = "Earlier")))
                    ),
                    systemInstruction = "Be concise.",
                    temperature = 0.3f,
                    onChunk = chunks::add
                )

            assertTrue(result.isSuccess)
            assertEquals("Hello there", result.getOrNull())
            assertEquals(listOf("Hello", " there"), chunks)

            val request = server.takeRequest()
            assertEquals("/api/chat", request.path)
            val payload = JSONObject(request.body.readUtf8())
            assertEquals("llama3.2", payload.getString("model"))
            assertTrue(payload.getBoolean("stream"))
            assertEquals(0.3, payload.getJSONObject("options").getDouble("temperature"), 0.001)
            val messages = payload.getJSONArray("messages")
            assertEquals("system", messages.getJSONObject(0).getString("role"))
            assertEquals("user", messages.getJSONObject(1).getString("role"))
            assertEquals("assistant", messages.getJSONObject(2).getString("role"))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `reports Ollama HTTP errors`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"error":"model not found"}""")
        )
        server.start()
        try {
            val result = OllamaService(server.url("/").toString(), "missing-model")
                .streamGenerate(
                    conversation = listOf(GeminiMessage("user", listOf(GeminiMessagePart(text = "Hi")))),
                    systemInstruction = null,
                    temperature = 0.4f,
                    onChunk = {}
                )

            assertFalse(result.isSuccess)
            assertEquals("model not found", result.exceptionOrNull()?.message)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `rejects invalid Ollama URL`() = runBlocking {
        val result = OllamaService("not a URL", "llama3.2")
            .streamGenerate(
                conversation = listOf(GeminiMessage("user", listOf(GeminiMessagePart(text = "Hi")))),
                systemInstruction = null,
                temperature = 0.4f,
                onChunk = {}
            )

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
