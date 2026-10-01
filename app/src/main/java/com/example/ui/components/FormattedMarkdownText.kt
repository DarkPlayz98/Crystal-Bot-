package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.CodeBorder
import com.example.ui.theme.CrystalCyanPrimary
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FormattedMarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val sections = parseMarkdownSections(text)

    Column(modifier = modifier) {
        sections.forEach { section ->
            when (section) {
                is MarkdownSection.CodeBlock -> {
                    CodeBlockCard(language = section.language, code = section.code)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                is MarkdownSection.Heading -> {
                    val style = when (section.level) {
                        1 -> MaterialTheme.typography.titleLarge.copy(
                            color = CrystalCyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        2 -> MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        else -> MaterialTheme.typography.titleSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = section.text,
                        style = style,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is MarkdownSection.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            color = CrystalCyanPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = parseInlineMarkdown(section.text),
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
                is MarkdownSection.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${section.number}. ",
                            color = CrystalCyanPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = parseInlineMarkdown(section.text),
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
                is MarkdownSection.Paragraph -> {
                    if (section.text.isNotBlank()) {
                        Text(
                            text = parseInlineMarkdown(section.text),
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CodeBackground)
            .border(1.dp, CodeBorder, RoundedCornerShape(10.dp))
    ) {
        // Code Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = language.ifBlank { "code" },
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(code))
                    Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("copy_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Code Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFE2E8F0),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

sealed class MarkdownSection {
    data class CodeBlock(val language: String, val code: String) : MarkdownSection()
    data class Heading(val level: Int, val text: String) : MarkdownSection()
    data class BulletItem(val text: String) : MarkdownSection()
    data class NumberedItem(val number: String, val text: String) : MarkdownSection()
    data class Paragraph(val text: String) : MarkdownSection()
}

fun parseMarkdownSections(raw: String): List<MarkdownSection> {
    val result = mutableListOf<MarkdownSection>()
    val lines = raw.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        if (line.trim().startsWith("```")) {
            val language = line.trim().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            result.add(MarkdownSection.CodeBlock(language, codeLines.joinToString("\n")))
            i++
            continue
        }

        if (line.startsWith("### ")) {
            result.add(MarkdownSection.Heading(3, line.removePrefix("### ").trim()))
        } else if (line.startsWith("## ")) {
            result.add(MarkdownSection.Heading(2, line.removePrefix("## ").trim()))
        } else if (line.startsWith("# ")) {
            result.add(MarkdownSection.Heading(1, line.removePrefix("# ").trim()))
        } else if (line.trim().startsWith("- ") || line.trim().startsWith("* ")) {
            result.add(MarkdownSection.BulletItem(line.trim().substring(2)))
        } else if (Regex("^\\d+\\.\\s+.*").matches(line.trim())) {
            val dotIdx = line.trim().indexOf('.')
            val num = line.trim().substring(0, dotIdx)
            val content = line.trim().substring(dotIdx + 1).trim()
            result.add(MarkdownSection.NumberedItem(num, content))
        } else {
            result.add(MarkdownSection.Paragraph(line))
        }
        i++
    }

    return result
}

fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        val codeRegex = Regex("`(.*?)`")

        val matches = (boldRegex.findAll(text).map { Triple(it.range, "bold", it.groupValues[1]) } +
                codeRegex.findAll(text).map { Triple(it.range, "code", it.groupValues[1]) })
            .sortedBy { it.first.first }

        for (m in matches) {
            val range = m.first
            if (range.first > cursor) {
                append(text.substring(cursor, range.first))
            }
            if (m.second == "bold") {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                    append(m.third)
                }
            } else if (m.second == "code") {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = DarkSurfaceElevated,
                        color = CrystalCyanPrimary
                    )
                ) {
                    append(" ${m.third} ")
                }
            }
            cursor = range.last + 1
        }
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
