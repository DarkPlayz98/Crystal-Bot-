package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val STRICT_EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$")

@Composable
fun WelcomeScreen(
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    onSignUpWithEmail: (email: String, pass: String) -> Unit,
    onContinueAsGuest: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var authModeTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Create Account
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var buddyTapCount by remember { mutableIntStateOf(0) }

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val trimmedEmail = email.trim()
    val isEmailValid by remember(trimmedEmail) {
        derivedStateOf {
            trimmedEmail.isNotBlank() && STRICT_EMAIL_REGEX.matches(trimmedEmail)
        }
    }

    val isPasswordValid by remember(password) {
        derivedStateOf { password.trim().length >= 6 }
    }

    val canSubmit by remember(isEmailValid, isPasswordValid, isLoading) {
        derivedStateOf { isEmailValid && isPasswordValid && !isLoading }
    }

    // Dynamic Buddy Greeting message based on context
    val buddySpeechText = remember(authModeTab, isEmailFocused, isPasswordFocused, buddyTapCount, errorMessage, isLoading) {
        when {
            isLoading -> "Connecting with Firebase Auth... ⏳"
            !errorMessage.isNullOrBlank() -> "Uh-oh! Check your details below! 💡"
            buddyTapCount > 0 && buddyTapCount % 4 == 1 -> "Yay! You tapped me! I'm your AI Buddy! 🤖✨"
            buddyTapCount > 0 && buddyTapCount % 4 == 2 -> "I can browse the web & run terminal commands! 🌐"
            buddyTapCount > 0 && buddyTapCount % 4 == 3 -> "Let's build awesome stuff together! 🚀"
            buddyTapCount > 0 && buddyTapCount % 4 == 0 -> "Log in below and let's explore! 🎉"
            isPasswordFocused -> "Shh! I'll protect your secret password! 🔒"
            isEmailFocused -> "What's your email, friend? 📧"
            authModeTab == 1 -> "Woohoo! Let's create your account! 🌟"
            else -> "Hey friend! I'm your AI buddy ✨ Tap me!"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("welcome_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ⭐ THE AI BUDDY MASCOT FRONT & CENTER! ⭐
            LoginBuddyMascot(
                speechText = buddySpeechText,
                isPasswordFocused = isPasswordFocused,
                onBuddyTap = {
                    buddyTapCount++
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // App Title & Tagline
            Text(
                text = "Crystal Bot",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Your autonomous AI companion with live cloud pods.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Firebase Authentication Pill with Soft Rounded Edges
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFEF3C7))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Firebase",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Firebase Auth Secured",
                    color = Color(0xFF92400E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Minimal White Card with Soft Rounded Corners (Minimal UI)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
                // Minimal Tab Switcher with Rounded Corners
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (authModeTab == 0) ObsidianBlack else Color.Transparent)
                            .border(1.dp, if (authModeTab == 0) BorderSubtle else Color.Transparent, RoundedCornerShape(16.dp))
                            .clickable { authModeTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            color = if (authModeTab == 0) CrystalCyanPrimary else TextSecondary,
                            fontWeight = if (authModeTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (authModeTab == 1) ObsidianBlack else Color.Transparent)
                            .border(1.dp, if (authModeTab == 1) BorderSubtle else Color.Transparent, RoundedCornerShape(16.dp))
                            .clickable { authModeTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Account",
                            color = if (authModeTab == 1) CrystalCyanPrimary else TextSecondary,
                            fontWeight = if (authModeTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }

                // Error Message Card with Rounded Edges
                AnimatedVisibility(
                    visible = !errorMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFB91C1C),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Email Input with Rounded Edges
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = TextSecondary) },
                    placeholder = { Text("you@example.com", color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = if (isEmailValid) CrystalCyanPrimary else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (trimmedEmail.isNotBlank()) {
                            if (isEmailValid) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Valid email",
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CrystalCyanPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isEmailFocused = it.isFocused }
                        .testTag("auth_email_input")
                )

                if (trimmedEmail.isNotBlank() && !isEmailValid) {
                    Text(
                        text = "Enter a valid email address (e.g. name@domain.com)",
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Password Input with Rounded Edges
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = TextSecondary) },
                    placeholder = { Text("At least 6 characters", color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CrystalCyanPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (canSubmit) {
                            if (authModeTab == 0) onSignInWithEmail(email, password)
                            else onSignUpWithEmail(email, password)
                        }
                    }),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isPasswordFocused = it.isFocused }
                        .testTag("auth_password_input")
                )

                if (password.isNotEmpty() && !isPasswordValid) {
                    Text(
                        text = "Password must be at least 6 characters",
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verified Demo Credentials Pill for Instant Testing
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .clickable {
                            email = "agent@crystal.bot"
                            password = "CrystalBot2026!"
                            authModeTab = 0
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verified Account: ",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "agent@crystal.bot",
                        color = CrystalCyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Auto-fill",
                        color = CrystalSkyBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Authentication Button with Pill Rounded Edges
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (authModeTab == 0) onSignInWithEmail(email, password)
                        else onSignUpWithEmail(email, password)
                    },
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrystalCyanPrimary,
                        disabledContainerColor = DarkSurfaceHighlight
                    ),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = if (authModeTab == 0) "Sign In" else "Create Account",
                            color = if (canSubmit) Color.White else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (canSubmit) Color.White else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Discrete Sandbox Guest link with Rounded Pill
            TextButton(
                onClick = onContinueAsGuest,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("auth_guest_button")
            ) {
                Text(
                    text = "Explore Sandbox Mode as Guest",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Animated, interactive AI Buddy Mascot for the Login Screen!
 * Blinks, bobs gently, reacts to taps, and displays a friendly speech bubble!
 */
@Composable
fun LoginBuddyMascot(
    speechText: String,
    isPasswordFocused: Boolean,
    onBuddyTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val bounceAnim = remember { Animatable(0f) }
    var eyeBlinkProgress by remember { mutableStateOf(1f) }

    // Floating bobbing motion
    val infiniteTransition = rememberInfiniteTransition(label = "buddy_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buddy_hover"
    )

    // Pulsing aura glow
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buddy_aura"
    )

    // Periodic natural blinking
    LaunchedEffect(Unit) {
        while (true) {
            delay(3200)
            // Blink closed
            eyeBlinkProgress = 0.1f
            delay(120)
            // Blink open
            eyeBlinkProgress = 1f
            delay(1800)
            // Quick double blink
            eyeBlinkProgress = 0.1f
            delay(100)
            eyeBlinkProgress = 1f
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cute Speech Bubble with Warm Peach Hue (Matching Screenshot)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFFFEDD5))
                .border(1.dp, Color(0xFFFED7AA), RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = speechText,
                color = Color(0xFF9A3412),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Small speech triangle pointing down to buddy
        Canvas(modifier = Modifier.size(12.dp, 6.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path, color = Color(0xFFFFEDD5))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Mascot Buddy Body with Float & Bounce
        Box(
            modifier = Modifier
                .offset(y = (floatOffset + bounceAnim.value).dp)
                .size(110.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    coroutineScope.launch {
                        bounceAnim.animateTo(-14f, tween(150, easing = FastOutSlowInEasing))
                        bounceAnim.animateTo(0f, tween(250, easing = FastOutSlowInEasing))
                    }
                    onBuddyTap()
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawInteractiveBuddy(
                    scope = this,
                    blinkProgress = if (isPasswordFocused) 0.2f else eyeBlinkProgress,
                    auraScale = auraScale,
                    isCheering = bounceAnim.value < -2f
                )
            }
        }
    }
}

/**
 * Draws the high-fidelity pearlescent 3D AI buddy with glowing crystal eyes,
 * sparkling highlight, rosy blush, and friendly smile!
 */
private fun drawInteractiveBuddy(
    scope: DrawScope,
    blinkProgress: Float,
    auraScale: Float,
    isCheering: Boolean
) {
    val w = scope.size.width
    val h = scope.size.height
    val center = Offset(w * 0.5f, h * 0.54f)

    // 1. Soft Warm Peach & Lavender Ambient Glow (Matching Screenshot)
    scope.drawCircle(
        color = Color(0x35FED7AA),
        radius = w * 0.46f * auraScale,
        center = center
    )
    scope.drawCircle(
        color = Color(0x25DDD6FE),
        radius = w * 0.42f * auraScale,
        center = center
    )

    // 2. Fluffy Organic Cloud Body Puffs (Matching Screenshot Character)
    val cloudBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF5F3FF),
            Color(0xFFEDE9FE),
            Color(0xFFDDD6FE),
            Color(0xFFC4B5FD)
        ),
        center = Offset(w * 0.42f, h * 0.44f),
        radius = w * 0.52f
    )

    // Main organic cloud puffs
    scope.drawCircle(brush = cloudBrush, radius = w * 0.30f, center = Offset(w * 0.50f, h * 0.54f))
    scope.drawCircle(brush = cloudBrush, radius = w * 0.23f, center = Offset(w * 0.29f, h * 0.58f))
    scope.drawCircle(brush = cloudBrush, radius = w * 0.23f, center = Offset(w * 0.71f, h * 0.58f))
    scope.drawCircle(brush = cloudBrush, radius = w * 0.22f, center = Offset(w * 0.35f, h * 0.42f))
    scope.drawCircle(brush = cloudBrush, radius = w * 0.24f, center = Offset(w * 0.65f, h * 0.42f))

    // 3. Cute Golden Star / Flower Badge on side (from screenshot)
    val starCenter = Offset(w * 0.24f, h * 0.38f)
    scope.drawCircle(
        color = Color(0xFFFBBF24),
        radius = w * 0.09f,
        center = starCenter
    )
    scope.drawCircle(
        color = Color(0xFFF59E0B),
        radius = w * 0.07f,
        center = starCenter
    )
    scope.drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = w * 0.03f,
        center = Offset(starCenter.x - 2f, starCenter.y - 2f)
    )

    // 4. Rosy Blush Cheeks (Warm Coral Pink)
    scope.drawCircle(
        color = Color(0x65FB7185),
        radius = w * 0.06f,
        center = Offset(w * 0.37f, h * 0.56f)
    )
    scope.drawCircle(
        color = Color(0x65FB7185),
        radius = w * 0.06f,
        center = Offset(w * 0.63f, h * 0.56f)
    )

    // 5. Expressive Happy Eyes
    val eyeHeight = (w * 0.075f) * blinkProgress
    val eyeWidth = w * 0.05f

    scope.drawRoundRect(
        color = Color(0xFF1C1917),
        topLeft = Offset(w * 0.43f - eyeWidth / 2, h * 0.51f - eyeHeight / 2),
        size = Size(eyeWidth, eyeHeight.coerceAtLeast(3f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(eyeWidth / 2, eyeWidth / 2)
    )
    scope.drawRoundRect(
        color = Color(0xFF1C1917),
        topLeft = Offset(w * 0.57f - eyeWidth / 2, h * 0.51f - eyeHeight / 2),
        size = Size(eyeWidth, eyeHeight.coerceAtLeast(3f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(eyeWidth / 2, eyeWidth / 2)
    )

    // 6. Sweet Cheerful Smile
    val smilePath = Path().apply {
        if (isCheering) {
            moveTo(w * 0.46f, h * 0.56f)
            quadraticTo(w * 0.50f, h * 0.63f, w * 0.54f, h * 0.56f)
            close()
        } else {
            moveTo(w * 0.46f, h * 0.56f)
            quadraticTo(w * 0.50f, h * 0.60f, w * 0.54f, h * 0.56f)
        }
    }
    if (isCheering) {
        scope.drawPath(smilePath, color = Color(0xFF1C1917))
    } else {
        scope.drawPath(
            smilePath,
            color = Color(0xFF1C1917),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
    }
}
