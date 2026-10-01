package com.example.ui.components

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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AgentProfile
import com.example.data.model.TerminalLog
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
import java.net.URLEncoder

private val BROWSER_BOOKMARKS = listOf(
    "Google" to "https://www.google.com",
    "Wikipedia" to "https://en.wikipedia.org",
    "Hacker News" to "https://news.ycombinator.com",
    "GitHub" to "https://github.com",
    "AI News" to "https://techmeme.com"
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AiComputerLiveWindow(
    isVisible: Boolean,
    agents: List<AgentProfile>,
    selectedAgentId: String,
    onSelectAgent: (String) -> Unit,
    currentUrl: String,
    onNavigateUrl: (String) -> Unit,
    terminalLogs: List<TerminalLog>,
    onRunTerminalCommand: (String) -> Unit,
    activeTaskDescription: String?,
    onOpenFullscreenComputer: () -> Unit,
    onCloseWindow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val selectedAgent = agents.find { it.id == selectedAgentId } ?: agents.firstOrNull()
    var windowMode by remember { mutableIntStateOf(0) } // 0 = Real Browser, 1 = Bash Terminal
    var commandInput by remember { mutableStateOf("") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var lastLoadedUrl by remember { mutableStateOf("") }
    var pageProgress by remember { mutableIntStateOf(0) }
    var isPageLoading by remember { mutableStateOf(false) }
    var pageTitle by remember { mutableStateOf("") }
    var urlBarText by remember(currentUrl) { mutableStateOf(currentUrl) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_dot"
    )

    fun navigateTo(raw: String) {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return
        val finalUrl = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://en.wikipedia.org/w/index.php?search=" + URLEncoder.encode(trimmed, "UTF-8")
        }
        urlBarText = finalUrl
        lastLoadedUrl = finalUrl
        onNavigateUrl(finalUrl)
        webViewInstance?.loadUrl(finalUrl)
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(340.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                .testTag("ai_computer_live_window")
        ) {
            // Window Header Bar with Sleek Rounded Edges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceCard)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A).copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Computer",
                        tint = CrystalCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Computer: ${selectedAgent?.name ?: "Agent"}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mode Tabs (Browser vs Terminal) + Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Browser Tab Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (windowMode == 0) ObsidianBlack else Color.Transparent)
                            .border(1.dp, if (windowMode == 0) BorderSubtle else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable { windowMode = 0 }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = if (windowMode == 0) CrystalCyanPrimary else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Web",
                                color = if (windowMode == 0) CrystalCyanPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Terminal Tab Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (windowMode == 1) ObsidianBlack else Color.Transparent)
                            .border(1.dp, if (windowMode == 1) BorderSubtle else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable { windowMode = 1 }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = if (windowMode == 1) CrystalCyanPrimary else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Terminal",
                                color = if (windowMode == 1) CrystalCyanPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Fullscreen Button
                    IconButton(
                        onClick = onOpenFullscreenComputer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expand Fullscreen",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close Button
                    IconButton(
                        onClick = onCloseWindow,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close window",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Agent Quick-Switcher Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianBlack)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                agents.forEach { agent ->
                    val isAgentSelected = agent.id == selectedAgent?.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isAgentSelected) Color(0xFFE0F2FE) else DarkSurfaceCard)
                            .border(
                                1.dp,
                                if (isAgentSelected) CrystalCyanPrimary else BorderSubtle,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectAgent(agent.id) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(agent.shape.color)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = agent.name,
                                color = if (isAgentSelected) CrystalCyanPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isAgentSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Real-Time AI Task Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceCard)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤖 AI Working: ",
                    color = CrystalCyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = activeTaskDescription ?: "Agent standing by in cloud computer. Ready for prompts.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = BorderSubtle)

            // Content Area (Browser or Terminal)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (windowMode == 0) {
                    // REAL INTERACTIVE WEBVIEW BROWSER
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Interactive Address Bar with Go & Nav Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceCard)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { webViewInstance?.goBack() },
                                enabled = canGoBack,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (canGoBack) TextPrimary else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = { webViewInstance?.goForward() },
                                enabled = canGoForward,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Forward",
                                    tint = if (canGoForward) TextPrimary else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = { webViewInstance?.reload() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload",
                                    tint = CrystalCyanPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Address input field
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ObsidianBlack)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "SSL",
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                BasicTextField(
                                    value = urlBarText,
                                    onValueChange = { urlBarText = it },
                                    textStyle = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    cursorBrush = SolidColor(CrystalCyanPrimary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                    keyboardActions = KeyboardActions(onGo = {
                                        focusManager.clearFocus()
                                        navigateTo(urlBarText)
                                    }),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                if (urlBarText.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            focusManager.clearFocus()
                                            navigateTo(urlBarText)
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Go",
                                            tint = CrystalCyanPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlBarText))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = "Open in Chrome",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Preset Bookmark Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceElevated)
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BROWSER_BOOKMARKS.forEach { (name, url) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkSurfaceCard)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable { navigateTo(url) }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = name,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (isPageLoading && pageProgress < 100) {
                            LinearProgressIndicator(
                                progress = { pageProgress / 100f },
                                modifier = Modifier.fillMaxWidth().height(1.5.dp),
                                color = CrystalCyanPrimary,
                                trackColor = DarkSurfaceCard
                            )
                        }

                        // Live WebView with full interactivity
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.White)
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
                                                    urlBarText = newUrl
                                                    lastLoadedUrl = newUrl
                                                    onNavigateUrl(newUrl)
                                                    view?.loadUrl(newUrl)
                                                }
                                                return false
                                            }

                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                isPageLoading = true
                                                url?.let {
                                                    urlBarText = it
                                                    lastLoadedUrl = it
                                                    onNavigateUrl(it)
                                                }
                                                canGoBack = view?.canGoBack() == true
                                                canGoForward = view?.canGoForward() == true
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                isPageLoading = false
                                                pageTitle = view?.title ?: ""
                                                url?.let {
                                                    urlBarText = it
                                                    lastLoadedUrl = it
                                                    onNavigateUrl(it)
                                                }
                                                canGoBack = view?.canGoBack() == true
                                                canGoForward = view?.canGoForward() == true
                                            }
                                        }
                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                pageProgress = newProgress
                                                if (newProgress >= 100) isPageLoading = false
                                            }
                                        }
                                        val target = currentUrl.ifBlank { "https://en.wikipedia.org/wiki/Artificial_intelligence" }
                                        lastLoadedUrl = target
                                        loadUrl(target)
                                        webViewInstance = this
                                    }
                                },
                                update = { view ->
                                    if (currentUrl.isNotBlank() && currentUrl != lastLoadedUrl) {
                                        lastLoadedUrl = currentUrl
                                        view.loadUrl(currentUrl)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // REAL BASH TERMINAL WITH AI EXECUTION
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(terminalLogs) { log ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$ ",
                                        color = selectedAgent?.shape?.color ?: Color(0xFF38BDF8),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = log.command,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                if (log.output.isNotBlank()) {
                                    Text(
                                        text = log.output,
                                        color = if (log.isError) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }

                        // Preset Command Chips with Rounded Corners
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
                                "curl news.ycombinator.com",
                                "browse wikipedia.org",
                                "ls -la",
                                "help",
                                "clear"
                            ).forEach { cmd ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                        .clickable { onRunTerminalCommand(cmd) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cmd,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Terminal Interactive Command Input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$ ",
                                color = selectedAgent?.shape?.color ?: Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            BasicTextField(
                                value = commandInput,
                                onValueChange = { commandInput = it },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(Color(0xFF38BDF8)),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (commandInput.isNotBlank()) {
                                        onRunTerminalCommand(commandInput.trim())
                                        commandInput = ""
                                    }
                                }),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(
                                onClick = {
                                    if (commandInput.isNotBlank()) {
                                        onRunTerminalCommand(commandInput.trim())
                                        commandInput = ""
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Run",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
