package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentChannel
import com.example.data.model.AgentProfile
import com.example.data.model.AgentShape
import com.example.data.model.ChannelCollabMessage
import com.example.ui.components.AgentAvatarView
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.CodeBorder
import com.example.ui.theme.CrystalCyanPrimary
import com.example.ui.theme.CrystalSkyBlue
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmOrangePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun AgentChannelsScreen(
    channels: List<AgentChannel>,
    selectedChannelId: String,
    onSelectChannel: (String) -> Unit,
    messages: List<ChannelCollabMessage>,
    allAgents: List<AgentProfile>,
    isSwarmActive: Boolean,
    onTriggerSwarmGoal: (String) -> Unit,
    onOpenBrowser: (agentId: String, url: String) -> Unit = { _, _ -> },
    onOpenOs: (agentId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedChannel = channels.find { it.id == selectedChannelId } ?: channels.first()
    var inputGoalText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("agent_channels_screen")
    ) {
        // Channel Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            channels.forEach { channel ->
                val isSelected = channel.id == selectedChannel.id
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) DarkSurfaceHighlight else Color.Transparent)
                        .border(1.dp, if (isSelected) CrystalCyanPrimary else BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { onSelectChannel(channel.id) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("channel_item_${channel.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = if (isSelected) CrystalCyanPrimary else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = channel.name,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Channel Info Header & Quick System Shortcuts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceCard)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${selectedChannel.name}",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedChannel.topic,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Jump to Live Browser & Bot OS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onOpenBrowser("research_bot", "") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = CrystalSkyBlue, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Browser", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onOpenOs("chief_of_staff") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Live OS", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Swarm autonomous status notice
        if (isSwarmActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CrystalCyanPrimary.copy(alpha = 0.1f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = CrystalCyanPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Autonomous inter-agent delegation in progress... (Chief of Staff orchestrating)",
                    color = CrystalCyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Channel Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        if (msg.isUser) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceHighlight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("You", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            AgentAvatarView(shape = msg.senderShape, size = 32.dp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = msg.senderName,
                                        color = if (msg.isUser) CrystalSkyBlue else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (msg.delegatedTo != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DarkSurfaceElevated)
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "routed to @${msg.delegatedTo}",
                                                color = CrystalCyanPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                Text(text = timeString, color = TextMuted, fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = msg.content,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )

                            // Action badge
                            if (msg.actionBadge != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceElevated)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(text = "⚡ ${msg.actionBadge}", color = TextSecondary, fontSize = 11.sp)
                                }
                            }

                            // 🌐 Live Browser Viewport Preview (Direct 1-tap jump to Real Browser)
                            if (msg.browserUrlPreview != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Language, contentDescription = null, tint = CrystalSkyBlue, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${msg.senderName} Live Web Session",
                                                    color = CrystalSkyBlue,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = msg.browserUrlPreview,
                                                color = TextSecondary,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                val agentId = if (msg.senderName.contains("Atlas", ignoreCase = true)) "research_bot" else "chief_of_staff"
                                                onOpenBrowser(agentId, msg.browserUrlPreview)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrystalCyanPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Open Browser", color = ObsidianBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 💻 Terminal & OS Output Preview snippet (with 1-tap jump to Live OS)
                            if (msg.terminalOutputPreview != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CodeBackground)
                                        .border(1.dp, CodeBorder, RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Ubuntu Container Execution", color = Color(0xFF22C55E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            TextButton(
                                                onClick = { onOpenOs("coding_bot") },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text("Open in OS", color = WarmOrangePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = msg.terminalOutputPreview,
                                            color = Color(0xFF86EFAC),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Swarm Workflow Starters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Deploy release to staging",
                "Scrape market research & post summary",
                "Fix failing test in authentication module"
            ).forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceHighlight)
                        .clickable { onTriggerSwarmGoal(preset) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("🤖 $preset", color = CrystalSkyBlue, fontSize = 11.sp)
                }
            }
        }

        // Swarm Message Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (inputGoalText.isEmpty()) {
                    Text(
                        text = "Assign task to team of bots (Chief will route)...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
                BasicTextField(
                    value = inputGoalText,
                    onValueChange = { inputGoalText = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(CrystalCyanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("channel_goal_input")
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputGoalText.isNotBlank()) {
                        onTriggerSwarmGoal(inputGoalText.trim())
                        inputGoalText = ""
                    }
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CrystalCyanPrimary)
                    .testTag("send_channel_goal_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = ObsidianBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
