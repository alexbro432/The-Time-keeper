package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    Surface(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.08f),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.12f)
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.05f)
                        )
                    }
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        color = if (isDark) {
            Color(0xFF1E293B).copy(alpha = 0.65f) // Deep glass slate
        } else {
            Color.White.copy(alpha = 0.72f) // Beautiful pure light glass
        },
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Deluxe slate/indigo moving blobs gradient matching the "High Density" aesthetic layout
    val brush = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F172A), // Slate 900
                Color(0xFF1E293B), // Slate 800
                Color(0xFF020617), // Slate 950
                Color(0xFF111827), // Gray 900
                Color(0xFF0F172A)
            ),
            start = androidx.compose.ui.geometry.Offset(animOffset / 2, 0f),
            end = androidx.compose.ui.geometry.Offset(animOffset, animOffset)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF8FAFC), // Slate 50
                Color(0xFFF2F2F7), // Light iOS background
                Color(0xFFE2E8F0), // Slate 200
                Color(0xFFF1F5F9), // Slate 100
                Color(0xFFF2F2F7)
            ),
            start = androidx.compose.ui.geometry.Offset(0f, animOffset / 3),
            end = androidx.compose.ui.geometry.Offset(animOffset, animOffset)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush)
    ) {
        // Floating decorative glowing orbs under the glass for elite depth effect (iOS style)
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-50).dp, y = 100.dp)
                .blur(80.dp)
                .background(
                    if (isDarkMode) Color(0xFF007AFF).copy(alpha = 0.15f * pulseScale)
                    else Color(0xFF007AFF).copy(alpha = 0.08f * pulseScale),
                    shape = RoundedCornerShape(125.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 200.dp, y = (-50).dp)
                .blur(90.dp)
                .background(
                    if (isDarkMode) Color(0xFFBF5AF2).copy(alpha = 0.12f * pulseScale)
                    else Color(0xFFBF5AF2).copy(alpha = 0.07f * pulseScale),
                    shape = RoundedCornerShape(150.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 100.dp, y = 500.dp)
                .blur(70.dp)
                .background(
                    if (isDarkMode) Color(0xFF30D158).copy(alpha = 0.1f * pulseScale)
                    else Color(0xFF34C759).copy(alpha = 0.06f * pulseScale),
                    shape = RoundedCornerShape(110.dp)
                )
        )

        content()
    }
}
