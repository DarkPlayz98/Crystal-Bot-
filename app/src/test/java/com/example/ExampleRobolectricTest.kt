package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GrokMode
import com.example.ui.components.parseMarkdownSections
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Crystal Bot", appName)
    }

    @Test
    fun `verify Grok modes configuration`() {
        val modes = GrokMode.entries
        assertTrue(modes.any { it == GrokMode.FAST })
        assertTrue(modes.any { it == GrokMode.THINK })
        assertTrue(modes.any { it == GrokMode.DEEP_SEARCH })
        assertTrue(modes.any { it == GrokMode.FUN })
        assertTrue(modes.any { it == GrokMode.IMAGE_GEN })
    }

    @Test
    fun `verify markdown parser separates code blocks and text`() {
        val raw = "Here is some code:\n```kotlin\nval x = 1\n```\nAnd a bullet:\n- Point A"
        val sections = parseMarkdownSections(raw)
        assertTrue(sections.isNotEmpty())
    }

    @Test
    fun `verify agent shapes and collaboration models`() {
        val shapes = com.example.data.model.AgentShape.entries
        assertTrue(shapes.any { it == com.example.data.model.AgentShape.WHITE_SPHERE })
        assertTrue(shapes.any { it == com.example.data.model.AgentShape.PURPLE_CIRCLE })
        assertTrue(shapes.any { it == com.example.data.model.AgentShape.PINK_TRIANGLE })
        assertTrue(shapes.any { it == com.example.data.model.AgentShape.CYAN_HEX })
        assertTrue(shapes.any { it == com.example.data.model.AgentShape.ORANGE_EGG })
    }

    @Test
    fun `verify auth user data class`() {
        val user = com.example.data.auth.AuthUser(
            uid = "test_uid_123",
            email = "agent@crystal.bot",
            isAnonymous = false,
            displayName = "Agent"
        )
        assertEquals("test_uid_123", user.uid)
        assertEquals("agent@crystal.bot", user.email)
    }
}
