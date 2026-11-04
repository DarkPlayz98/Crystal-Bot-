package com.example.data.model

enum class ChatProvider(val id: String, val label: String) {
    GEMINI("gemini", "Gemini"),
    OLLAMA("ollama", "Ollama (local)");

    companion object {
        fun fromId(id: String?): ChatProvider =
            entries.firstOrNull { it.id == id } ?: GEMINI
    }
}
