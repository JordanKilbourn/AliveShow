package com.playworld.aliveshow.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Temporary placeholder: draws a rounded shape and fake “lights”.
 * amplitude drives a glow; booleans toggle sides. Replace with Filament 3D later.
 */
@Composable
fun TeslaModelView(
    amplitude: Float,
    headlight: Boolean,
    drl: Boolean,
    fog: Boolean,
    tail: Boolean,
) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val glow = (0.15f + 0.85f * amplitude).coerceIn(0f, 1f)

        // car body blob
        drawRoundRect(Color(0xFF1E1F24), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f))

        // headlights (front left/right)
        val frontY = h * 0.35f
        val fx = w * 0.18f
        val rx = w * 0.82f

        if (headlight || drl || fog) {
            val white = Color(1f, 1f, 1f, 0.25f + 0.55f * glow)
            if (headlight) {
                drawCircle(white, radius = 28f + 20f * glow, center = androidx.compose.ui.geometry.Offset(fx, frontY))
                drawCircle(white, radius = 28f + 20f * glow, center = androidx.compose.ui.geometry.Offset(rx, frontY))
            }
            if (drl || fog) {
                drawCircle(white, radius = 12f + 12f * glow, center = androidx.compose.ui.geometry.Offset(fx, frontY + 36f))
                drawCircle(white, radius = 12f + 12f * glow, center = androidx.compose.ui.geometry.Offset(rx, frontY + 36f))
            }
        }

        // tail lights (rear left/right)
        if (tail) {
            val red = Color(1f, 0f, 0f, 0.25f + 0.55f * glow)
            val rearY = h * 0.70f
            drawCircle(red, radius = 22f + 16f * glow, center = androidx.compose.ui.geometry.Offset(fx, rearY))
            drawCircle(red, radius = 22f + 16f * glow, center = androidx.compose.ui.geometry.Offset(rx, rearY))
        }
    }
}
