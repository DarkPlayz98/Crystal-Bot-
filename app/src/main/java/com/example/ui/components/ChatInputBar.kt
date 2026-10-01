package com.example.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
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
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    activeMode: GrokMode,
    onModeClick: () -> Unit,
    attachedBitmap: Bitmap?,
    onBitmapAttached: (Bitmap?) -> Unit,
    isGenerating: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAttachMenu by remember { mutableStateOf(false) }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onBitmapAttached(bitmap)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ObsidianBlack)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Attached Image Preview Pill
        if (attachedBitmap != null) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = attachedBitmap.asImageBitmap(),
                    contentDescription = "Attached Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Attached for analysis",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = { onBitmapAttached(null) },
                    modifier = Modifier.size(24.dp).testTag("remove_attached_image")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Floating Grok-Style Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(26.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach / Plus Button
                Box {
                    IconButton(
                        onClick = { showAttachMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_input_attach_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add attachment or tool",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showAttachMenu,
                        onDismissRequest = { showAttachMenu = false },
                        modifier = Modifier.background(DarkSurfaceHighlight)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Take Camera Photo", color = TextPrimary) },
                            onClick = {
                                showAttachMenu = false
                                takePhotoLauncher.launch()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Image, contentDescription = null, tint = CrystalCyanPrimary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("/imagine Image Generation", color = TextPrimary) },
                            onClick = {
                                showAttachMenu = false
                                onInputChange("/imagine ")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Image, contentDescription = null, tint = activeMode.badgeColor)
                            }
                        )
                    }
                }

                // Active Mode Chip inside input
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(activeMode.badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, activeMode.badgeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { onModeClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("input_active_mode_chip"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = activeMode.icon,
                            contentDescription = null,
                            tint = activeMode.badgeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeMode.shortLabel,
                            color = activeMode.badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = when (activeMode) {
                                GrokMode.IMAGE_GEN -> "Describe the image to create..."
                                GrokMode.DEEP_SEARCH -> "Search anything with DeepSearch..."
                                GrokMode.THINK -> "Ask a complex reasoning question..."
                                GrokMode.FUN -> "Ask anything (prepare to be roasted)..."
                                else -> "Ask anything or /imagine..."
                            },
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(CrystalCyanPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_text_field"),
                        maxLines = 5
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Send / Stop Button
                if (isGenerating) {
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceHighlight)
                            .testTag("chat_input_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Generating",
                            tint = CrystalCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    val canSend = inputText.isNotBlank() || attachedBitmap != null
                    IconButton(
                        onClick = onSend,
                        enabled = canSend,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (canSend) CrystalCyanPrimary else DarkSurfaceHighlight)
                            .testTag("chat_input_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Send",
                            tint = if (canSend) ObsidianBlack else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
