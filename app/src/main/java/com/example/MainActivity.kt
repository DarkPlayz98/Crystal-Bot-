package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ChatMessage
import com.example.ui.components.AiComputerLiveWindow
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.CrystalAppBar
import com.example.ui.components.HistoryDrawerContent
import com.example.ui.components.HomeEmptyState
import com.example.ui.components.ModeSelectorSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.components.WorkspaceBottomBar
import com.example.ui.screens.AgentChannelsScreen
import com.example.ui.screens.CloudComputersScreen
import com.example.ui.screens.TeachAndRoutinesScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.CrystalBotTheme
import com.example.ui.theme.ObsidianBlack
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.WorkspaceTab
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrystalBotTheme {
                MainAppHost()
            }
        }
    }
}

@Composable
fun MainAppHost(
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.isSignedIn,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "auth_transition"
    ) { signedIn ->
        if (!signedIn) {
            WelcomeScreen(
                onSignInWithEmail = { email, pass -> viewModel.signInWithEmail(email, pass) },
                onSignUpWithEmail = { email, pass -> viewModel.signUpWithEmail(email, pass) },
                onContinueAsGuest = { viewModel.signInAnonymously() },
                isLoading = uiState.isAuthLoading,
                errorMessage = uiState.authErrorMessage
            )
        } else {
            CrystalWorkspaceRoot(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrystalWorkspaceRoot(
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val modeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val totalItems = uiState.messages.size + if (uiState.isGenerating) 1 else 0
    LaunchedEffect(totalItems, uiState.streamingText.length) {
        if (totalItems > 0 && uiState.activeWorkspaceTab == WorkspaceTab.CHAT) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HistoryDrawerContent(
                sessions = uiState.sessions,
                currentSessionId = uiState.currentSessionId,
                onSelectSession = { id ->
                    viewModel.selectSession(id)
                    viewModel.setWorkspaceTab(WorkspaceTab.CHAT)
                    scope.launch { drawerState.close() }
                },
                onNewChat = {
                    viewModel.resetToHomeScreen()
                    viewModel.setWorkspaceTab(WorkspaceTab.CHAT)
                    scope.launch { drawerState.close() }
                },
                onTogglePin = { id, pinned ->
                    viewModel.togglePinSession(id, pinned)
                },
                onRenameSession = { id, title ->
                    viewModel.renameSession(id, title)
                },
                onDeleteSession = { id ->
                    viewModel.deleteSession(id)
                },
                onClearAll = {
                    viewModel.clearAllHistory()
                    scope.launch { drawerState.close() }
                },
                onOpenSettings = {
                    viewModel.setShowSettingsDialog(true)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBlack)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .testTag("crystal_workspace_scaffold"),
            containerColor = ObsidianBlack,
            topBar = {
                CrystalAppBar(
                    selectedMode = uiState.activeMode,
                    isAiComputerWindowOpen = uiState.isAiComputerWindowOpen,
                    onToggleAiComputerWindow = {
                        viewModel.toggleAiComputerWindow()
                    },
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onModeClick = {
                        viewModel.setShowModeSheet(true)
                    },
                    onNewChatClick = {
                        viewModel.resetToHomeScreen()
                        viewModel.setWorkspaceTab(WorkspaceTab.CHAT)
                    },
                    onSettingsClick = {
                        viewModel.setShowSettingsDialog(true)
                    }
                )
            },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Chat Input Bar only visible when in Chat tab
                    if (uiState.activeWorkspaceTab == WorkspaceTab.CHAT) {
                        ChatInputBar(
                            inputText = uiState.inputText,
                            onInputChange = { viewModel.setInputText(it) },
                            activeMode = uiState.activeMode,
                            onModeClick = { viewModel.setShowModeSheet(true) },
                            attachedBitmap = uiState.attachedBitmap,
                            onBitmapAttached = { viewModel.setAttachedBitmap(it) },
                            isGenerating = uiState.isGenerating,
                            onSend = { viewModel.sendMessage() },
                            onStop = { viewModel.stopGenerating() }
                        )
                    }

                    // Workspace 5-Tab Navigation Bar
                    WorkspaceBottomBar(
                        currentTab = uiState.activeWorkspaceTab,
                        onTabSelected = { tab -> viewModel.setWorkspaceTab(tab) }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ObsidianBlack)
            ) {
                // 🖥️ 100% Live Real-Time AI Computer Window (Embedded directly in workspace)
                AiComputerLiveWindow(
                    isVisible = uiState.isAiComputerWindowOpen,
                    agents = uiState.agents,
                    selectedAgentId = uiState.selectedCloudAgentId,
                    onSelectAgent = { viewModel.selectCloudAgent(it) },
                    currentUrl = uiState.browserUrls[uiState.selectedCloudAgentId] ?: "https://en.wikipedia.org/wiki/Artificial_intelligence",
                    onNavigateUrl = { viewModel.navigateBrowserUrl(it) },
                    terminalLogs = uiState.terminalLogs[uiState.selectedCloudAgentId] ?: emptyList(),
                    onRunTerminalCommand = { viewModel.runTerminalCommand(it) },
                    activeTaskDescription = uiState.activeAiTaskDescription,
                    onOpenFullscreenComputer = {
                        viewModel.setAiComputerWindowOpen(false)
                        viewModel.setWorkspaceTab(WorkspaceTab.CLOUD_COMPUTERS)
                    },
                    onCloseWindow = { viewModel.setAiComputerWindowOpen(false) }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(ObsidianBlack)
                ) {
                    when (uiState.activeWorkspaceTab) {
                        WorkspaceTab.CHAT -> {
                            // 1. CHAT TAB
                            if (uiState.messages.isEmpty() && !uiState.isGenerating) {
                                HomeEmptyState(
                                    selectedMode = uiState.activeMode,
                                    onModeSelect = { viewModel.setActiveMode(it) },
                                    onPromptClick = { prompt, mode ->
                                        viewModel.setActiveMode(mode)
                                        viewModel.sendMessage(forcedPrompt = prompt, forcedMode = mode)
                                    },
                                    onOpenLiveComputer = {
                                        viewModel.setAiComputerWindowOpen(true)
                                    }
                                )
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("messages_lazy_column")
                                ) {
                                    items(uiState.messages, key = { it.id }) { message ->
                                        ChatMessageItem(
                                            message = message,
                                            isSpeakingThisMessage = uiState.isSpeaking && uiState.currentlySpeakingText == message.content,
                                            onSpeakToggle = { text -> viewModel.toggleSpeak(text) },
                                            onRegenerate = { viewModel.regenerateLastMessage() },
                                            onOpenComputerWindow = {
                                                viewModel.setAiComputerWindowOpen(true)
                                            }
                                        )
                                    }

                                    if (uiState.isGenerating) {
                                        item(key = "streaming_current_message") {
                                            val streamingSourcesJson = uiState.streamingSources?.let { list ->
                                                val arr = JSONArray()
                                                for (s in list) {
                                                    val obj = JSONObject()
                                                    obj.put("title", s.title)
                                                    obj.put("url", s.url)
                                                    obj.put("domain", s.domain)
                                                    obj.put("snippet", s.snippet)
                                                    arr.put(obj)
                                                }
                                                arr.toString()
                                            }
                                            val placeholderMsg = ChatMessage(
                                                id = "streaming_in_progress",
                                                sessionId = uiState.currentSessionId ?: "",
                                                role = "assistant",
                                                content = uiState.streamingText.ifEmpty { "..." },
                                                reasoning = uiState.streamingReasoning,
                                                sourcesJson = streamingSourcesJson,
                                                modelBadge = uiState.activeMode.title
                                            )
                                            ChatMessageItem(
                                                message = placeholderMsg,
                                                isStreamingThisMessage = true,
                                                onOpenComputerWindow = {
                                                    viewModel.setAiComputerWindowOpen(true)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    WorkspaceTab.CHANNELS -> {
                        // 2. INTER-AGENT COLLABORATION SWARMS
                        AgentChannelsScreen(
                            channels = uiState.channels,
                            selectedChannelId = uiState.selectedChannelId,
                            onSelectChannel = { viewModel.selectChannel(it) },
                            messages = uiState.channelMessages,
                            allAgents = uiState.agents,
                            isSwarmActive = uiState.isSwarmActive,
                            onTriggerSwarmGoal = { viewModel.triggerSwarmGoal(it) },
                            onOpenBrowser = { agentId, url -> viewModel.openBrowserForAgent(agentId, url) },
                            onOpenOs = { agentId -> viewModel.openOsForAgent(agentId) }
                        )
                    }

                    WorkspaceTab.CLOUD_COMPUTERS -> {
                        // 3. PERSISTENT CLOUD COMPUTERS (Terminal, Browser, Files, Live OS)
                        val activeAgentId = uiState.selectedCloudAgentId
                        val logs = uiState.terminalLogs[activeAgentId] ?: emptyList()
                        val currentUrl = uiState.browserUrls[activeAgentId] ?: "https://en.wikipedia.org/wiki/Artificial_intelligence"
                        val files = uiState.fileTrees[activeAgentId] ?: uiState.fileTrees["coding_bot"] ?: emptyList()

                        CloudComputersScreen(
                            agents = uiState.agents,
                            selectedAgentId = activeAgentId,
                            onSelectAgent = { viewModel.selectCloudAgent(it) },
                            terminalLogs = logs,
                            onRunCommand = { viewModel.runTerminalCommand(it) },
                            currentUrl = currentUrl,
                            onNavigateUrl = { viewModel.navigateBrowserUrl(it) },
                            fileTree = files,
                            initialTab = uiState.selectedComputerTab,
                            onTabSelected = { viewModel.setComputerTab(it) }
                        )
                    }

                    WorkspaceTab.ROUTINES -> {
                        // 4. TEACH-A-TASK & AUTOMATED ROUTINES
                        TeachAndRoutinesScreen(
                            routines = uiState.routines,
                            onToggleRoutine = { id, enabled -> viewModel.toggleRoutine(id, enabled) },
                            onRunRoutineNow = { viewModel.runRoutineNow(it) },
                            onCreateNewRoutine = { name, trigger, desc, steps ->
                                viewModel.createNewRoutine(name, trigger, desc, steps)
                            }
                        )
                    }
                }
            }
        }
    }
}

    // Mode Selector Sheet
    if (uiState.showModeSheet) {
        ModeSelectorSheet(
            sheetState = modeSheetState,
            currentMode = uiState.activeMode,
            onModeSelected = { mode ->
                viewModel.setActiveMode(mode)
            },
            onDismiss = {
                viewModel.setShowModeSheet(false)
            }
        )
    }

    // Settings Dialog
    if (uiState.showSettingsDialog) {
        SettingsDialog(
            currentApiKey = uiState.customApiKey,
            onSaveApiKey = { key ->
                viewModel.setCustomApiKey(key)
            },
            speechRate = uiState.speechRate,
            onSpeechRateChange = { rate ->
                viewModel.setSpeechRate(rate)
            },
            onSignOut = {
                viewModel.signOut()
            },
            onDismiss = {
                viewModel.setShowSettingsDialog(false)
            }
        )
    }
}
