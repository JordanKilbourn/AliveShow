package com.playworld.aliveshow.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Palette
private val Midnight = Color(0xFF0A0F1E)
private val DeepNavy = Color(0xFF0E162B)
private val Accent = Color(0xFFFF6A00)      // brand orange
private val AccentAlt = Color(0xFF7C9EFF)   // electric blue
private val TextPrimary = Color(0xFFEAF0FF)

// Material 3 dark scheme
private val Scheme = darkColorScheme(
    primary = Accent,
    secondary = AccentAlt,
    background = Midnight,
    surface = DeepNavy,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = Color.Black
)

// Softer, rounder shapes
private val AppShapes = Shapes(
    extraLarge = RoundedCornerShape(28.dp),
    large = RoundedCornerShape(24.dp),
    medium = RoundedCornerShape(18.dp),
    small = RoundedCornerShape(12.dp)
)

@Composable
fun AliveTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, shapes = AppShapes, content = content)
}

// Subtle vertical gradient for the whole app background
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF0A0F1E),
                    Color(0xFF0F1B38),
                    Color(0xFF0B1222)
                )
            )
        ),
        content = content
    )
}

// “Glass” card: translucent surface, soft border & shadow
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, Color(0x22FFFFFF)),
        shape = AppShapes.large,
        content = { content() }
    )
}
