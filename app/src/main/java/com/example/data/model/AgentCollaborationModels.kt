package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class AgentShape(
    val shapeName: String,
    val color: Color
) {
    WHITE_SPHERE("White Sphere Mascot", Color(0xFFF8FAFC)),
    PURPLE_CIRCLE("Purple Orb", Color(0xFF8B5CF6)),
    PINK_TRIANGLE("Pink Prism", Color(0xFFEC4899)),
    ORANGE_EGG("Orange Pebble", Color(0xFFF97316)),
    CYAN_HEX("Cyan Hexagon", Color(0xFF06B6D4)),
    BLUE_TEARDROP("Blue Teardrop", Color(0xFF3B82F6)),
    YELLOW_ARCH("Yellow Arch", Color(0xFFEAB308)),
    SLATE_CYLINDER("Slate Block", Color(0xFF64748B))
}

data class AgentProfile(
    val id: String,
    val name: String,
    val title: String,
    val description: String,
    val shape: AgentShape,
    val status: AgentStatus = AgentStatus.RUNNING,
    val ipAddress: String,
    val cpuPercent: Int = 18,
    val ramMb: Int = 1420
)

enum class AgentStatus {
    RUNNING, IDLE, BUSY
}

data class TerminalLog(
    val command: String,
    val output: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CloudFile(
    val path: String,
    val name: String,
    val size: String,
    val isDirectory: Boolean = false,
    val content: String = ""
)

data class BrowserTabState(
    val url: String = "https://internal.agent.cloud",
    val title: String = "Cloud Workspace Dashboard",
    val isLoading: Boolean = false,
    val contentSummary: String = "Agent Browser Sandbox initialized with headless Chromium v124.",
    val screenshotPlaceholderColor: Color = Color(0xFF1E293B)
)

data class BotCloudComputer(
    val agentId: String,
    val browserState: BrowserTabState,
    val terminalLogs: List<TerminalLog>,
    val fileTree: List<CloudFile>,
    val osName: String = "Ubuntu 24.04 LTS (x86_64)",
    val uptimeHours: Int = 74
)

data class AgentChannel(
    val id: String,
    val name: String,
    val topic: String,
    val agentIds: List<String>,
    val unreadCount: Int = 0
)

data class ChannelCollabMessage(
    val id: String,
    val channelId: String,
    val senderName: String,
    val senderShape: AgentShape,
    val isUser: Boolean = false,
    val content: String,
    val delegatedTo: String? = null,
    val actionBadge: String? = null,
    val terminalOutputPreview: String? = null,
    val browserUrlPreview: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class TeachableSkillRoutine(
    val id: String,
    val title: String,
    val description: String,
    val triggerSchedule: String, // e.g. "Every day at 9:00 AM" or "On Git Push"
    val isCron: Boolean = true,
    val assignedAgentName: String,
    val assignedAgentShape: AgentShape,
    val isEnabled: Boolean = true,
    val recordedSteps: List<String>,
    val lastRunText: String = "12m ago • Succeeded",
    val executionCount: Int = 42
)
