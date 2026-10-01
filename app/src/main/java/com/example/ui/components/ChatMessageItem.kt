package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessage
import com.example.data.model.AgentShape
import com.example.data.model.WebSource
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrystalCyanPrimary
import com.example.ui.theme.CrystalSkyBlue
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isStreamingThisMessage: Boolean = false,
    isSpeakingThisMessage: Boolean = false,
    onSpeakToggle: (String) -> Unit = {},
    onRegenerate: () -> Unit = {},
    onOpenComputerWindow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var userFeedback by remember { mutableStateOf<Int?>(null) } // 1 for up, -1 for down
    var hasCopied by remember { mutableStateOf(false) }
    var isReasoningExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(hasCopied) {
        if (hasCopied) {
            delay(2000)
            hasCopied = false
        }
    }

    val isUser = message.role == "user"
    val timeString = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    if (isUser) {
        // User Message: Premium Right-Aligned Capsule
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                    .background(Color(0xFFE0F2FE))
                    .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("user_message_${message.id}")
            ) {
                if (message.imageUrl != null) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceCard)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📷 Attached Image", color = CrystalCyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Text(
                    text = message.content,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeString,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    } else {
        // Assistant Message: Grok-Inspired Clean Conversational Layout
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("bot_message_${message.id}")
        ) {
            // Header Bar: Bot Mascot Avatar, Name, Mode Chip & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Buddy Mascot Avatar
                    AgentAvatarView(
                        shape = AgentShape.WHITE_SPHERE,
                        size = 30.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Crystal",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    message.modelBadge?.let { badge ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                color = CrystalSkyBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Text(
                    text = timeString,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grok-style Thinking Accordion (for Think / Reasoning Mode)
            if (!message.reasoning.isNullOrBlank()) {
                val thinkingSeconds = if (message.thinkingDurationMs != null && message.thinkingDurationMs > 0) {
                    String.format(Locale.US, "%.1fs", message.thinkingDurationMs / 1000.0)
                } else "3.2s"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isReasoningExpanded = !isReasoningExpanded }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = CrystalCyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thought for $thinkingSeconds",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = if (isReasoningExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isReasoningExpanded) "Collapse" else "Expand",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isReasoningExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = message.reasoning,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // DeepSearch Sources List (if search sources present)
            val parsedSources = remember(message.sourcesJson) {
                parseSources(message.sourcesJson)
            }
            if (parsedSources.isNotEmpty()) {
                WebSourcesList(
                    sources = parsedSources,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // Image Generation Card (if visual model generated an image)
            if (message.isImageGeneration && !message.imageUrl.isNullOrBlank()) {
                ImageDisplayCard(
                    imageUrl = message.imageUrl,
                    prompt = message.content,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // Core Response Text with High-Fidelity Markdown & Code Formatting
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp)
            ) {
                FormattedMarkdownText(text = message.content)
            }

            // Pulsing Streaming Indicator
            if (isStreamingThisMessage) {
                val infiniteTransition = rememberInfiniteTransition(label = "streaming_pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_alpha"
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CrystalCyanPrimary.copy(alpha = alpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Generating response...",
                        color = CrystalSkyBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, CrystalCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { onOpenComputerWindow() }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "View AI Computer",
                            tint = CrystalCyanPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View Computer",
                            color = CrystalCyanPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Grok Message Action Row (Copy, Read Aloud, Thumbs Up/Down, Regenerate, Share)
            if (!isStreamingThisMessage) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.content))
                            hasCopied = true
                            Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("copy_bot_msg_${message.id}")
                    ) {
                        Icon(
                            imageVector = if (hasCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = if (hasCopied) Color(0xFF22C55E) else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // TTS Voice Audio Playback
                    IconButton(
                        onClick = { onSpeakToggle(message.content) },
                        modifier = Modifier.size(32.dp).testTag("speak_bot_msg_${message.id}")
                    ) {
                        Icon(
                            imageVector = if (isSpeakingThisMessage) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeakingThisMessage) "Stop speaking" else "Read aloud",
                            tint = if (isSpeakingThisMessage) CrystalCyanPrimary else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Thumbs Up
                    IconButton(
                        onClick = {
                            userFeedback = if (userFeedback == 1) null else 1
                            Toast.makeText(context, "Thanks for the feedback!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("thumb_up_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Good response",
                            tint = if (userFeedback == 1) CrystalSkyBlue else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Thumbs Down
                    IconButton(
                        onClick = {
                            userFeedback = if (userFeedback == -1) null else -1
                            Toast.makeText(context, "Feedback noted", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("thumb_down_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Needs improvement",
                            tint = if (userFeedback == -1) Color(0xFFEF4444) else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Regenerate
                    IconButton(
                        onClick = onRegenerate,
                        modifier = Modifier.size(32.dp).testTag("regenerate_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate answer",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, message.content)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Crystal Response"))
                        },
                        modifier = Modifier.size(32.dp).testTag("share_bot_msg_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { onOpenComputerWindow() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "View AI Computer",
                            tint = CrystalCyanPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Computer",
                            color = CrystalSkyBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun parseSources(jsonString: String?): List<WebSource> {
    if (jsonString.isNullOrBlank()) return emptyList()
    return try {
        val array = JSONArray(jsonString)
        val list = mutableListOf<WebSource>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                WebSource(
                    title = obj.optString("title", "Source"),
                    url = obj.optString("url", ""),
                    domain = obj.optString("domain", "web"),
                    snippet = obj.optString("snippet", "")
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}
