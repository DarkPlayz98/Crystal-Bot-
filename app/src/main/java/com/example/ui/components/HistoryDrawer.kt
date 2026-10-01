package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSession
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
fun HistoryDrawerContent(
    sessions: List<ChatSession>,
    currentSessionId: String?,
    onSelectSession: (String) -> Unit,
    onNewChat: () -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onRenameSession: (String, String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearAll: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var renameSessionTarget by remember { mutableStateOf<ChatSession?>(null) }
    var renameText by remember { mutableStateOf("") }

    val filteredSessions = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) sessions
        else sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val pinnedList = filteredSessions.filter { it.isPinned }
    val recentList = filteredSessions.filter { !it.isPinned }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(DarkSurfaceElevated)
            .padding(top = 40.dp, bottom = 16.dp, start = 12.dp, end = 12.dp)
            .testTag("history_drawer")
    ) {
        // New Chat Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CrystalCyanPrimary)
                .clickable { onNewChat() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("drawer_new_chat_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = ObsidianBlack,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Chat",
                color = ObsidianBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceHighlight)
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search conversations...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(CrystalCyanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("drawer_search_input"),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chat List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (pinnedList.isNotEmpty()) {
                item {
                    Text(
                        text = "PINNED",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    )
                }
                items(pinnedList, key = { it.id }) { session ->
                    SessionRowItem(
                        session = session,
                        isSelected = session.id == currentSessionId,
                        onSelect = { onSelectSession(session.id) },
                        onTogglePin = { onTogglePin(session.id, !session.isPinned) },
                        onRename = {
                            renameSessionTarget = session
                            renameText = session.title
                        },
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }

            item {
                Text(
                    text = "RECENT",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                )
            }

            if (recentList.isEmpty() && pinnedList.isEmpty()) {
                item {
                    Text(
                        text = "No conversations yet",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(recentList, key = { it.id }) { session ->
                    SessionRowItem(
                        session = session,
                        isSelected = session.id == currentSessionId,
                        onSelect = { onSelectSession(session.id) },
                        onTogglePin = { onTogglePin(session.id, !session.isPinned) },
                        onRename = {
                            renameSessionTarget = session
                            renameText = session.title
                        },
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }
        }

        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onClearAll,
                modifier = Modifier.testTag("drawer_clear_all_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear History", color = TextMuted, fontSize = 12.sp)
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("drawer_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Rename Dialog
    if (renameSessionTarget != null) {
        AlertDialog(
            onDismissRequest = { renameSessionTarget = null },
            title = { Text("Rename Chat", color = TextPrimary, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = renameSessionTarget
                        if (target != null && renameText.isNotBlank()) {
                            onRenameSession(target.id, renameText.trim())
                        }
                        renameSessionTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrystalCyanPrimary)
                ) {
                    Text("Save", color = ObsidianBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameSessionTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
fun SessionRowItem(
    session: ChatSession,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) DarkSurfaceHighlight else DarkSurfaceElevated)
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag("session_item_${session.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = if (isSelected) CrystalCyanPrimary else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = session.title,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(DarkSurfaceHighlight)
            ) {
                DropdownMenuItem(
                    text = { Text(if (session.isPinned) "Unpin" else "Pin", color = TextPrimary) },
                    onClick = {
                        showMenu = false
                        onTogglePin()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (session.isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename", color = TextPrimary) },
                    onClick = {
                        showMenu = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFEF4444)) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                    }
                )
            }
        }
    }
}
