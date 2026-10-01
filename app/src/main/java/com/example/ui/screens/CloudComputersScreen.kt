package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import com.example.data.model.AgentProfile
import com.example.data.model.CloudFile
import com.example.data.model.TerminalLog
import com.example.data.termux.TermuxEngine
import com.example.data.termux.TermuxExecutionResult
import com.example.ui.components.AgentAvatarView
import com.example.ui.components.BotOsManagementView
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.CodeBorder
import com.example.ui.theme.CrystalCyanPrimary
import com.example.ui.theme.CrystalSkyBlue
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmOrangePrimary
import com.example.ui.theme.WarmSunriseGlow
import java.net.URLEncoder

private val BROWSER_PRESETS = listOf(
    "Hacker News" to "https://news.ycombinator.com",
    "Wikipedia" to "https://en.wikipedia.org/wiki/Artificial_intelligence",
    "GitHub" to "https://github.com",
    "Techmeme" to "https://www.techmeme.com",
    "Google" to "https://www.google.com"
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CloudComputersScreen(
    agents: List<AgentProfile>,
    selectedAgentId: String,
    onSelectAgent: (String) -> Unit,
    terminalLogs: List<TerminalLog>,
    onRunCommand: (String) -> Unit,
    currentUrl: String,
    onNavigateUrl: (String) -> Unit,
    fileTree: List<CloudFile>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val selectedAgent = agents.find { it.id == selectedAgentId } ?: agents.first()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Terminal, 1 = Browser, 2 = File System, 3 = Bot OS
    var isTermuxRouteActive by remember { mutableStateOf(false) }
    val isTermuxInstalled = remember { TermuxEngine.isTermuxAppInstalled(context) }
    var commandInput by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<CloudFile?>(null) }
    var browserUrlInput by remember(currentUrl) { mutableStateOf(currentUrl) }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isBlank()) return
        if (isTermuxRouteActive) {
            val routeResult = TermuxEngine.routeCommand(context, trimmed)
            when (routeResult) {
                is TermuxExecutionResult.SentToTermuxApp -> {
                    Toast.makeText(context, "Routed to Termux: $trimmed", Toast.LENGTH_SHORT).show()
                }
                is TermuxExecutionResult.ExecutedLocally -> {
                    Toast.makeText(context, "Local Shell execution complete", Toast.LENGTH_SHORT).show()
                }
                is TermuxExecutionResult.TermuxNotInstalled -> {
                    Toast.makeText(context, "Termux not installed. Running in container.", Toast.LENGTH_SHORT).show()
                }
                is TermuxExecutionResult.Error -> {
                    Toast.makeText(context, routeResult.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        onRunCommand(trimmed)
    }

    // Real WebView State
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var pageTitle by remember { mutableStateOf("") }
    var pageProgress by remember { mutableIntStateOf(0) }
    var isPageLoading by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isHudExpanded by remember { mutableStateOf(false) }

    fun navigateTo(rawUrl: String) {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return
        val finalUrl = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=" + URLEncoder.encode(trimmed, "UTF-8")
        }
        browserUrlInput = finalUrl
        onNavigateUrl(finalUrl)
        webViewInstance?.loadUrl(finalUrl)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("cloud_computers_screen")
    ) {
        // Agent Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            agents.forEach { agent ->
                val isSelected = agent.id == selectedAgent.id
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) DarkSurfaceHighlight else Color.Transparent)
                        .border(1.dp, if (isSelected) agent.shape.color else BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable { onSelectAgent(agent.id) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("cloud_agent_tab_${agent.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AgentAvatarView(shape = agent.shape, size = 22.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = agent.name,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                }
            }
        }

        // Cloud Pod Hardware Telemetry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceCard)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = selectedAgent.shape.color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "${selectedAgent.name} • ${selectedAgent.title}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "IP: ${selectedAgent.ipAddress} • Ubuntu 24.04 LTS Container",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = CrystalSkyBlue, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "${selectedAgent.cpuPercent}% CPU", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "${selectedAgent.ramMb} MB", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // 3-Tab Selector: Terminal, Browser, File System
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurfaceElevated,
            contentColor = CrystalCyanPrimary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CrystalCyanPrimary,
                    height = 2.dp
                )
            },
            divider = { HorizontalDivider(color = BorderSubtle) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bash Terminal", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Real Browser", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("100% LIVE", color = Color(0xFF22C55E), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("File System", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bot OS (Termux)", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(WarmSunriseGlow)
                                .border(1.dp, Color(0xFFFED7AA), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("PROOT", color = WarmOrangePrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> {
                    // TERMINAL TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ObsidianBlack)
                            .padding(12.dp)
                    ) {
                        // Termux Bridge & Routing Bar (Warm Pastel Pill Styling)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isTermuxInstalled) Color(0xFF22C55E) else WarmOrangePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isTermuxRouteActive) "Termux Route: ACTIVE" else "Termux Route: READY",
                                                color = if (isTermuxRouteActive) Color(0xFF15803D) else TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isTermuxInstalled) "• com.termux" else "• Emulated OS",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Text(
                                            text = if (isTermuxRouteActive) "Routing commands to native Termux app" else "Route commands directly to Termux app",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { selectedTab = 3 },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("OS Guide", color = WarmOrangePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Switch(
                                        checked = isTermuxRouteActive,
                                        onCheckedChange = { active ->
                                            isTermuxRouteActive = active
                                            val msg = if (active) {
                                                if (isTermuxInstalled) "Termux routing ON: commands route to com.termux" else "Termux routing ON (Virtual container mode)"
                                            } else {
                                                "Termux routing OFF"
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = WarmOrangePrimary,
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = DarkSurfaceCard
                                        ),
                                        modifier = Modifier.size(width = 44.dp, height = 24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Terminal Console Window - Deep Sleek Console with High Contrast & Rounded Edges
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(terminalLogs) { log ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$ ",
                                            color = selectedAgent.shape.color,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = log.command,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (log.output.isNotBlank()) {
                                        Text(
                                            text = log.output,
                                            color = if (log.isError) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Command Preset Chips with Rounded Corners
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "how to make os using termux",
                                "bot-os status",
                                "proot-distro login ubuntu",
                                "neofetch",
                                "ai \"what can you do?\"",
                                "python main.py",
                                "pkg install -y proot-distro",
                                "curl news.ycombinator.com",
                                "browse wikipedia.org",
                                "ls -la",
                                "help",
                                "clear"
                            ).forEach { cmd ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(DarkSurfaceCard)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                                        .clickable { executeTerminalCommand(cmd) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = cmd,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Interactive Command Input with Rounded Edges
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$ ",
                                color = selectedAgent.shape.color,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            BasicTextField(
                                value = commandInput,
                                onValueChange = { commandInput = it },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(CrystalCyanPrimary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (commandInput.isNotBlank()) {
                                        executeTerminalCommand(commandInput.trim())
                                        commandInput = ""
                                    }
                                }),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("terminal_command_input"),
                                singleLine = true
                            )
                            IconButton(
                                onClick = {
                                    if (commandInput.isNotBlank()) {
                                        executeTerminalCommand(commandInput.trim())
                                        commandInput = ""
                                    }
                                },
                                modifier = Modifier.size(28.dp).testTag("terminal_send_command")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Run",
                                    tint = CrystalCyanPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // 100% REAL LIVE ANDROID WEBVIEW BROWSER TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ObsidianBlack)
                    ) {
                        // Browser Address & Control Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            // Row 1: URL input with Lock & Go
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceCard)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secure SSL",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BasicTextField(
                                    value = browserUrlInput,
                                    onValueChange = { browserUrlInput = it },
                                    textStyle = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    cursorBrush = SolidColor(CrystalCyanPrimary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                    keyboardActions = KeyboardActions(onGo = {
                                        focusManager.clearFocus()
                                        navigateTo(browserUrlInput)
                                    }),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("browser_address_input"),
                                    singleLine = true
                                )
                                if (browserUrlInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = { browserUrlInput = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        navigateTo(browserUrlInput)
                                    },
                                    modifier = Modifier.size(28.dp).testTag("browser_navigate_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Go",
                                        tint = CrystalCyanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Row 2: Nav Controls (Back, Forward, Reload, Open External, Telemetry Toggle)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { webViewInstance?.goBack() },
                                        enabled = canGoBack,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = if (canGoBack) TextPrimary else TextMuted.copy(alpha = 0.4f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { webViewInstance?.goForward() },
                                        enabled = canGoForward,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Forward",
                                            tint = if (canGoForward) TextPrimary else TextMuted.copy(alpha = 0.4f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { webViewInstance?.reload() },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh",
                                            tint = CrystalCyanPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(browserUrlInput))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // ignore
                                            }
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = "Open in Chrome",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Quick Telemetry Badge
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkSurfaceCard)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                        .clickable { isHudExpanded = !isHudExpanded }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isPageLoading) CrystalCyanPrimary else Color(0xFF22C55E))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${selectedAgent.name} Sandbox",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = if (isHudExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Preset Bookmark Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BROWSER_PRESETS.forEach { (name, url) ->
                                    val isCurrent = browserUrlInput.contains(url.removePrefix("https://").substringBefore('/'))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCurrent) DarkSurfaceHighlight else DarkSurfaceCard)
                                            .border(1.dp, if (isCurrent) CrystalCyanPrimary else BorderSubtle, RoundedCornerShape(8.dp))
                                            .clickable { navigateTo(url) }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = name,
                                            color = if (isCurrent) CrystalCyanPrimary else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // Real Progress Bar
                        if (isPageLoading && pageProgress < 100) {
                            LinearProgressIndicator(
                                progress = { pageProgress / 100f },
                                modifier = Modifier.fillMaxWidth().height(2.dp),
                                color = CrystalCyanPrimary,
                                trackColor = DarkSurfaceCard
                            )
                        }

                        // Collapsible Agent Telemetry Inspector HUD
                        AnimatedVisibility(visible = isHudExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, BorderSubtle)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🤖 Agent Autonomous Session",
                                        color = CrystalSkyBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Active DOM: 100% Real",
                                        color = Color(0xFF22C55E),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Controller: ${selectedAgent.name} (${selectedAgent.title})\nPage Title: ${pageTitle.ifBlank { "Live Web Viewport" }}\nAutomation: Headless cookies loaded • DOM Mutation Observer online",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // REAL 100% LIVE ANDROID WEBVIEW EMBEDDED
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.White)
                                .testTag("real_agent_webview_container")
                        ) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            setSupportZoom(true)
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                            cacheMode = WebSettings.LOAD_DEFAULT
                                        }

                                        webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                request?.url?.toString()?.let { newUrl ->
                                                    browserUrlInput = newUrl
                                                    onNavigateUrl(newUrl)
                                                    view?.loadUrl(newUrl)
                                                }
                                                return false
                                            }

                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                isPageLoading = true
                                                url?.let {
                                                    browserUrlInput = it
                                                    onNavigateUrl(it)
                                                }
                                                canGoBack = view?.canGoBack() == true
                                                canGoForward = view?.canGoForward() == true
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                isPageLoading = false
                                                pageTitle = view?.title ?: ""
                                                canGoBack = view?.canGoBack() == true
                                                canGoForward = view?.canGoForward() == true
                                                url?.let {
                                                    browserUrlInput = it
                                                    onNavigateUrl(it)
                                                }
                                            }
                                        }

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                pageProgress = newProgress
                                                if (newProgress >= 100) isPageLoading = false
                                            }

                                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                                pageTitle = title ?: ""
                                            }
                                        }

                                        loadUrl(currentUrl.ifBlank { "https://news.ycombinator.com" })
                                        webViewInstance = this
                                    }
                                },
                                update = { view ->
                                    if (view.url != currentUrl && currentUrl.isNotBlank()) {
                                        view.loadUrl(currentUrl)
                                    }
                                }
                            )

                            // Loading overlay for very first second
                            if (isPageLoading && pageProgress < 20) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(ObsidianBlack.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = CrystalCyanPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // FILE SYSTEM TAB
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ObsidianBlack)
                    ) {
                        // File list
                        Column(
                            modifier = Modifier
                                .width(160.dp)
                                .background(DarkSurfaceElevated)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "WORKSPACE FILES",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            LazyColumn {
                                items(fileTree) { file ->
                                    val isFileSelected = selectedFile?.path == file.path
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isFileSelected) DarkSurfaceHighlight else Color.Transparent)
                                            .clickable { selectedFile = file }
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                                            contentDescription = null,
                                            tint = if (file.isDirectory) CrystalCyanPrimary else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = file.name,
                                            color = if (isFileSelected) TextPrimary else TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // File Viewer / Editor
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(CodeBackground)
                                .padding(12.dp)
                        ) {
                            val activeFile = selectedFile ?: fileTree.firstOrNull()
                            if (activeFile != null) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = activeFile.path,
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = activeFile.size,
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = CodeBorder)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = activeFile.content,
                                        color = Color(0xFFE2E8F0),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // BOT OS (TERMUX PROOT) TAB
                    BotOsManagementView(
                        agentId = selectedAgent.id,
                        agentName = selectedAgent.name,
                        onRunCommandInTerminal = { cmd ->
                            executeTerminalCommand(cmd)
                            selectedTab = 0
                        }
                    )
                }
            }
        }
    }
}
