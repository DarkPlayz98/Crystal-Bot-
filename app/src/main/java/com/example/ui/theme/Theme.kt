package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CrystalColorScheme = lightColorScheme(
    primary = CrystalCyanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = CrystalSkyBlue,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceCard,
    onSecondaryContainer = TextPrimary,
    tertiary = ModeFunOrange,
    onTertiary = Color.White,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = DarkSurfaceElevated,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderFocus
)

@Composable
fun CrystalBotTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CrystalColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CrystalBotTheme(content = content)
}
