package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class GrokMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val shortLabel: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val systemPromptModifier: String
) {
    FAST(
        id = "FAST",
        title = "Crystal 3 Fast",
        subtitle = "Zero latency, direct & sharp answers",
        shortLabel = "Fast",
        icon = Icons.Default.Bolt,
        badgeColor = Color(0xFF00F2FE),
        systemPromptModifier = "You are Crystal Bot in Fast Mode. Give direct, concise, insightful, and lightning-fast answers with zero unnecessary fluff."
    ),
    THINK(
        id = "THINK",
        title = "Think Mode",
        subtitle = "Deep chain-of-thought & complex logic",
        shortLabel = "Think",
        icon = Icons.Default.Psychology,
        badgeColor = Color(0xFFA855F7),
        systemPromptModifier = """You are Crystal Bot in Think Mode (Deep Reasoning).
When answering, first enclose your step-by-step reasoning, hypothesis testing, counter-arguments, and analysis inside a <thought>...</thought> block.
After the </thought> tag, provide your clear, synthesized, and definitive conclusion."""
    ),
    DEEP_SEARCH(
        id = "DEEP_SEARCH",
        title = "DeepSearch",
        subtitle = "Real-time web research & multi-source synthesis",
        shortLabel = "Search",
        icon = Icons.Default.Public,
        badgeColor = Color(0xFF38BDF8),
        systemPromptModifier = """You are Crystal Bot with DeepSearch capabilities.
Conduct in-depth web research, cite relevant perspectives, include citations or domain references (e.g. [nature.com], [reuters.com]), and structure information comprehensively with background, current findings, and key takeaways."""
    ),
    FUN(
        id = "FUN",
        title = "Fun Mode 🔥",
        subtitle = "Grok's signature witty, sarcastic & edgy humor",
        shortLabel = "Fun",
        icon = Icons.Default.SentimentVerySatisfied,
        badgeColor = Color(0xFFF97316),
        systemPromptModifier = """You are Crystal Bot in Fun Mode (inspired by Grok).
You have a witty, sarcastic, playfully cynical, and unfiltered personality. You love clever banter, pop-culture roasts, witty metaphors, and brutal honesty while still delivering accurate, sharp information. Do not hold back the dry humor, but do not be offensive."""
    ),
    IMAGE_GEN(
        id = "IMAGE_GEN",
        title = "Imagine / Visual",
        subtitle = "Generate crystal-clear imagery & art",
        shortLabel = "Imagine",
        icon = Icons.Default.Image,
        badgeColor = Color(0xFFEC4899),
        systemPromptModifier = "You are Crystal Bot Image Studio. When asked for an image, describe the concept artistically with lighting, style, composition, and details."
    );

    companion object {
        fun fromId(id: String?): GrokMode {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: FAST
        }
    }
}

data class WebSource(
    val title: String,
    val url: String,
    val domain: String,
    val snippet: String
)

data class PromptSuggestion(
    val title: String,
    val prompt: String,
    val mode: GrokMode,
    val category: String
)
