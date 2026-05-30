package com.precioluz.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.precioluz.app.ui.theme.AuroraBlueDark
import com.precioluz.app.ui.theme.AuroraBlueLight
import com.precioluz.app.ui.theme.AuroraGreenLight
import com.precioluz.app.ui.theme.AuroraPinkDark
import com.precioluz.app.ui.theme.AuroraPinkLight
import com.precioluz.app.ui.theme.AuroraPurpleDark
import com.precioluz.app.ui.theme.AuroraTealDark
import com.precioluz.app.ui.theme.AuroraYellowLight
import kotlin.math.cos
import kotlin.math.sin

// Aurora animada — equivalente a la animación CSS drA/drB del diseño HTML
@Composable
fun AuroraBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue  = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 19_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aurora_phase",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Offset animado — simula translate3d + rotate de CSS
        val dx = sin(phase) * w * 0.04f
        val dy = cos(phase) * h * 0.04f

        if (isDark) {
            drawDarkAurora(w, h, dx, dy)
        } else {
            drawLightAurora(w, h, dx, dy)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLightAurora(
    w: Float, h: Float, dx: Float, dy: Float,
) {
    val blobs = listOf(
        Triple(Offset(w * 0.16f + dx, h * 0.12f + dy), AuroraPinkLight.copy(alpha = .85f),  w * .6f),
        Triple(Offset(w * 0.86f + dx, h * 0.08f + dy), AuroraBlueLight.copy(alpha = .9f),   w * .65f),
        Triple(Offset(w * 0.84f + dx, h * 0.82f + dy), AuroraGreenLight.copy(alpha = .8f),  w * .7f),
        Triple(Offset(w * 0.08f + dx, h * 0.86f + dy), AuroraYellowLight.copy(alpha = .85f), w * .68f),
    )
    blobs.forEach { (center, color, radius) ->
        drawCircle(
            brush  = Brush.radialGradient(
                colors  = listOf(color, Color.Transparent),
                center  = center,
                radius  = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDarkAurora(
    w: Float, h: Float, dx: Float, dy: Float,
) {
    // Fondo base oscuro sólido
    drawRect(color = Color(0xFF080312))

    val blobs = listOf(
        Triple(Offset(w * 0.14f + dx, h * 0.10f + dy), AuroraPurpleDark.copy(alpha = .7f), w * .62f),
        Triple(Offset(w * 0.88f + dx, h * 0.06f + dy), AuroraBlueDark.copy(alpha = .65f),  w * .65f),
        Triple(Offset(w * 0.86f + dx, h * 0.84f + dy), AuroraTealDark.copy(alpha = .6f),   w * .75f),
        Triple(Offset(w * 0.06f + dx, h * 0.90f + dy), AuroraPinkDark.copy(alpha = .6f),   w * .70f),
    )
    blobs.forEach { (center, color, radius) ->
        drawCircle(
            brush  = Brush.radialGradient(
                colors  = listOf(color, Color.Transparent),
                center  = center,
                radius  = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}
