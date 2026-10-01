package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrystalCyanPrimary
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.WorkspaceTab

@Composable
fun WorkspaceBottomBar(
    currentTab: WorkspaceTab,
    onTabSelected: (WorkspaceTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, BorderSubtle, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WorkspaceNavItem(
            title = "Chat",
            icon = Icons.Default.ChatBubble,
            isSelected = currentTab == WorkspaceTab.CHAT,
            onClick = { onTabSelected(WorkspaceTab.CHAT) }
        )
        WorkspaceNavItem(
            title = "Swarms",
            icon = Icons.Default.Groups,
            isSelected = currentTab == WorkspaceTab.CHANNELS,
            onClick = { onTabSelected(WorkspaceTab.CHANNELS) }
        )
        WorkspaceNavItem(
            title = "Cloud Pods",
            icon = Icons.Default.Computer,
            isSelected = currentTab == WorkspaceTab.CLOUD_COMPUTERS,
            onClick = { onTabSelected(WorkspaceTab.CLOUD_COMPUTERS) }
        )
        WorkspaceNavItem(
            title = "Routines",
            icon = Icons.Default.Schedule,
            isSelected = currentTab == WorkspaceTab.ROUTINES,
            onClick = { onTabSelected(WorkspaceTab.ROUTINES) }
        )
        WorkspaceNavItem(
            title = "Apps",
            icon = Icons.Default.Extension,
            isSelected = currentTab == WorkspaceTab.INTEGRATIONS,
            onClick = { onTabSelected(WorkspaceTab.INTEGRATIONS) }
        )
    }
}

@Composable
fun WorkspaceNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) androidx.compose.ui.graphics.Color(0xFFE0F2FE) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("nav_tab_${title.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) CrystalCyanPrimary else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = if (isSelected) CrystalCyanPrimary else TextMuted,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
