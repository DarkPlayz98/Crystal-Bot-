package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GrokMode
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrystalCyanPrimary
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CrystalAppBar(
    selectedMode: GrokMode,
    isAiComputerWindowOpen: Boolean = false,
    onToggleAiComputerWindow: () -> Unit = {},
    onMenuClick: () -> Unit,
    onModeClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ObsidianBlack)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Hamburger Drawer
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.testTag("app_bar_menu_button")
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Chat History",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Center: Mode Selector Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                .clickable { onModeClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("app_bar_mode_selector"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = selectedMode.icon,
                    contentDescription = null,
                    tint = selectedMode.badgeColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedMode.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select Mode",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Right Actions: Live Computer Window Toggle, New Chat & Settings
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Live AI Computer Toggle Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isAiComputerWindowOpen) DarkSurfaceHighlight else DarkSurfaceElevated)
                    .border(
                        1.dp,
                        if (isAiComputerWindowOpen) CrystalCyanPrimary else BorderSubtle,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onToggleAiComputerWindow() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("app_bar_computer_window_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Toggle AI Computer Window",
                        tint = if (isAiComputerWindowOpen) CrystalCyanPrimary else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier.testTag("app_bar_new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("app_bar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
