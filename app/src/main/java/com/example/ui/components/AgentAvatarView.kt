package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AgentShape

@Composable
fun AgentAvatarView(
    shape: AgentShape,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            when (shape) {
                AgentShape.WHITE_SPHERE -> drawWhiteSphereMascot(this)
                AgentShape.PURPLE_CIRCLE -> drawPurpleCircle(this)
                AgentShape.PINK_TRIANGLE -> drawPinkTriangle(this)
                AgentShape.ORANGE_EGG -> drawOrangeEgg(this)
                AgentShape.CYAN_HEX -> drawCyanHex(this)
                AgentShape.BLUE_TEARDROP -> drawBlueTeardrop(this)
                AgentShape.YELLOW_ARCH -> drawYellowArch(this)
                AgentShape.SLATE_CYLINDER -> drawSlateCylinder(this)
            }
        }
    }
}

// 1. Crystal Bot Sphere Mascot (Refined with crystalline sheen and cyan eye glints)
private fun drawWhiteSphereMascot(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height
    val center = Offset(w * 0.5f, h * 0.5f)
    val radius = w * 0.46f

    // Outer ambient crystal glow
    scope.drawCircle(
        color = Color(0x3300F2FE),
        radius = radius * 1.08f,
        center = center
    )

    // 3D pearlescent sphere gradient with subtle diamond tint
    scope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF0F9FF),
                Color(0xFFE0F2FE),
                Color(0xFFBAE6FD),
                Color(0xFF64748B)
            ),
            center = Offset(w * 0.35f, h * 0.35f),
            radius = radius * 1.3f
        ),
        radius = radius,
        center = center
    )

    // Crystal facet highlight path across top quadrant
    val facetPath = Path().apply {
        moveTo(w * 0.30f, h * 0.22f)
        lineTo(w * 0.50f, h * 0.16f)
        lineTo(w * 0.65f, h * 0.24f)
        lineTo(w * 0.45f, h * 0.28f)
        close()
    }
    scope.drawPath(facetPath, color = Color.White.copy(alpha = 0.65f))

    // Left tilted pill eye (with subtle dark-obsidian and cyan edge)
    scope.drawLine(
        color = Color(0xFF0F172A),
        start = Offset(w * 0.42f, h * 0.40f),
        end = Offset(w * 0.54f, h * 0.54f),
        strokeWidth = w * 0.13f,
        cap = StrokeCap.Round
    )
    scope.drawCircle(
        color = Color(0xFF00F2FE),
        radius = w * 0.025f,
        center = Offset(w * 0.44f, h * 0.42f)
    )

    // Right tilted pill eye
    scope.drawLine(
        color = Color(0xFF0F172A),
        start = Offset(w * 0.64f, h * 0.32f),
        end = Offset(w * 0.73f, h * 0.41f),
        strokeWidth = w * 0.12f,
        cap = StrokeCap.Round
    )
    scope.drawCircle(
        color = Color(0xFF00F2FE),
        radius = w * 0.022f,
        center = Offset(w * 0.66f, h * 0.34f)
    )

    // Specular crystal sparkle
    scope.drawCircle(
        color = Color.White,
        radius = w * 0.045f,
        center = Offset(w * 0.36f, h * 0.30f)
    )
}

// 2. Purple Circle
private fun drawPurpleCircle(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height
    val center = Offset(w * 0.5f, h * 0.5f)
    val radius = w * 0.44f

    scope.drawCircle(
        color = Color(0xFF8B5CF6),
        radius = radius,
        center = center
    )

    // Two vertical pill eyes
    val eyeColor = Color.White
    scope.drawRoundRect(
        color = eyeColor,
        topLeft = Offset(w * 0.38f, h * 0.38f),
        size = Size(w * 0.08f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
    )
    scope.drawRoundRect(
        color = eyeColor,
        topLeft = Offset(w * 0.54f, h * 0.38f),
        size = Size(w * 0.08f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
    )
}

// 3. Pink Triangle
private fun drawPinkTriangle(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.12f)
        lineTo(w * 0.88f, h * 0.84f)
        lineTo(w * 0.12f, h * 0.84f)
        close()
    }

    scope.drawPath(path, color = Color(0xFFEC4899))

    // Two dot eyes
    scope.drawCircle(color = Color.White, radius = w * 0.055f, center = Offset(w * 0.43f, h * 0.55f))
    scope.drawCircle(color = Color.White, radius = w * 0.055f, center = Offset(w * 0.57f, h * 0.55f))
}

// 4. Orange Pebble / Egg
private fun drawOrangeEgg(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    scope.drawOval(
        color = Color(0xFFF97316),
        topLeft = Offset(w * 0.12f, h * 0.14f),
        size = Size(w * 0.76f, h * 0.76f)
    )

    // Specular shine
    scope.drawOval(
        color = Color.White.copy(alpha = 0.8f),
        topLeft = Offset(w * 0.28f, h * 0.25f),
        size = Size(w * 0.16f, h * 0.24f)
    )
    scope.drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = w * 0.04f,
        center = Offset(w * 0.48f, h * 0.48f)
    )
}

// 5. Cyan Hexagon
private fun drawCyanHex(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.12f)
        lineTo(w * 0.86f, h * 0.32f)
        lineTo(w * 0.86f, h * 0.72f)
        lineTo(w * 0.5f, h * 0.90f)
        lineTo(w * 0.14f, h * 0.72f)
        lineTo(w * 0.14f, h * 0.32f)
        close()
    }
    scope.drawPath(path, color = Color(0xFF06B6D4))

    // Two angled slit eyes
    scope.drawLine(
        color = Color.White,
        start = Offset(w * 0.40f, h * 0.46f),
        end = Offset(w * 0.48f, h * 0.54f),
        strokeWidth = w * 0.07f,
        cap = StrokeCap.Round
    )
    scope.drawLine(
        color = Color.White,
        start = Offset(w * 0.56f, h * 0.46f),
        end = Offset(w * 0.64f, h * 0.54f),
        strokeWidth = w * 0.07f,
        cap = StrokeCap.Round
    )
}

// 6. Blue Teardrop
private fun drawBlueTeardrop(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.12f)
        cubicTo(w * 0.9f, h * 0.5f, w * 0.85f, h * 0.88f, w * 0.5f, h * 0.88f)
        cubicTo(w * 0.15f, h * 0.88f, w * 0.1f, h * 0.5f, w * 0.5f, h * 0.12f)
        close()
    }
    scope.drawPath(path, color = Color(0xFF3B82F6))

    // Two cute slanted dot eyes
    scope.drawCircle(color = Color.White, radius = w * 0.06f, center = Offset(w * 0.42f, h * 0.62f))
    scope.drawCircle(color = Color.White, radius = w * 0.06f, center = Offset(w * 0.58f, h * 0.62f))
}

// 7. Yellow Arch
private fun drawYellowArch(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    val path = Path().apply {
        moveTo(w * 0.20f, h * 0.86f)
        lineTo(w * 0.20f, h * 0.45f)
        cubicTo(w * 0.20f, h * 0.15f, w * 0.80f, h * 0.15f, w * 0.80f, h * 0.45f)
        lineTo(w * 0.80f, h * 0.86f)
        close()
    }
    scope.drawPath(path, color = Color(0xFFEAB308))

    // Two pill eyes
    scope.drawCircle(color = Color(0xFF713F12), radius = w * 0.06f, center = Offset(w * 0.42f, h * 0.52f))
    scope.drawCircle(color = Color(0xFF713F12), radius = w * 0.06f, center = Offset(w * 0.58f, h * 0.52f))
}

// 8. Slate Cylinder
private fun drawSlateCylinder(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    scope.drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(w * 0.16f, h * 0.20f),
        size = Size(w * 0.68f, h * 0.64f),
        cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
    )

    // Two slanted white eyes
    scope.drawLine(
        color = Color.White,
        start = Offset(w * 0.40f, h * 0.44f),
        end = Offset(w * 0.46f, h * 0.54f),
        strokeWidth = w * 0.065f,
        cap = StrokeCap.Round
    )
    scope.drawLine(
        color = Color.White,
        start = Offset(w * 0.56f, h * 0.44f),
        end = Offset(w * 0.62f, h * 0.54f),
        strokeWidth = w * 0.065f,
        cap = StrokeCap.Round
    )
}
