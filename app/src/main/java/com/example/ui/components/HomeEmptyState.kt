package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentShape
import com.example.data.model.GrokMode
import com.example.data.model.PromptSuggestion
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrystalCyanPrimary
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val SAMPLE_PROMPTS = listOf(
    PromptSuggestion(
        title = "Roast my life choices",
        prompt = "Roast my daily routine: I wake up at 11am, scroll social media for 2 hours, and call it 'networking'.",
        mode = GrokMode.FUN,
        category = "Humor"
    ),
    PromptSuggestion(
        title = "Quantum Entanglement",
        prompt = "Explain quantum entanglement and EPR paradox with rigorous first principles.",
        mode = GrokMode.THINK,
        category = "Physics"
    ),
    PromptSuggestion(
        title = "DeepSearch: Fusion Power",
        prompt = "Synthesize recent milestones and commercial timelines for nuclear fusion reactors.",
        mode = GrokMode.DEEP_SEARCH,
        category = "Research"
    ),
    PromptSuggestion(
        title = "Cyberpunk Crystal City",
        prompt = "/imagine A neon-lit cyberpunk metropolis made of luminous obsidian crystals in rain, ultra-detailed 8k",
        mode = GrokMode.IMAGE_GEN,
        category = "Visual"
    ),
    PromptSuggestion(
        title = "Optimize Algorithm",
        prompt = "Show an ultra-high-throughput concurrent queue implementation with explanation.",
        mode = GrokMode.FAST,
        category = "Coding"
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeEmptyState(
    selectedMode: GrokMode,
    onModeSelect: (GrokMode) -> Unit,
    onPromptClick: (String, GrokMode) -> Unit,
    onOpenLiveComputer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Mascot Buddy
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE))
                .border(2.dp, CrystalCyanPrimary.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AgentAvatarView(
                shape = AgentShape.WHITE_SPHERE,
                size = 70.dp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Crystal Bot",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Truth-seeking, high-speed intelligence without limits.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Live AI Computer Launcher Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, CrystalCyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable { onOpenLiveComputer() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Computer,
                contentDescription = null,
                tint = CrystalCyanPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Live AI Computer Window (Browser & Terminal)",
                color = CrystalCyanPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Switcher Pills
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GrokMode.entries.forEach { mode ->
                val isSelected = mode == selectedMode
                Row(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) mode.badgeColor.copy(alpha = 0.2f) else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) mode.badgeColor else BorderSubtle,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onModeSelect(mode) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("empty_state_mode_${mode.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = mode.icon,
                        contentDescription = null,
                        tint = mode.badgeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = mode.shortLabel,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Starter Prompt Suggestions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Explore Capabilities",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SAMPLE_PROMPTS.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { onPromptClick(item.prompt, item.mode) }
                        .padding(14.dp)
                        .testTag("prompt_suggestion_$index"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(item.mode.badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.mode.shortLabel,
                                    color = item.mode.badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.prompt,
                            color = TextMuted,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
