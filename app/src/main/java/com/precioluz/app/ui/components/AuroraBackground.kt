package com.precioluz.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.precioluz.app.ui.theme.AuroraBlueDark
import com.precioluz.app.ui.theme.AuroraBlueLight
import com.precioluz.app.ui.theme.AuroraGreenLight
import com.precioluz.app.ui.theme.AuroraPinkDark
import com.precioluz.app.ui.theme.AuroraPinkLight
import com.precioluz.app.ui.theme.AuroraPurpleDark
import com.precioluz.app.ui.theme.AuroraTealDark
import com.precioluz.app.ui.theme.AuroraYellowLight
import com.precioluz.app.ui.theme.BgDark
import kotlin.math.cos
import kotlin.math.sin

// Aurora muy suave: el fondo de la web es plano, aquí solo queda un halo tenue
// que da personalidad a la app sin competir con el contenido.
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
            animation  = tween(durationMillis = 28_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aurora_phase",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val dx = sin(phase) * w * 0.02f
        val dy = cos(phase) * h * 0.02f

        if (isDark) drawDarkAurora(w, h, dx, dy) else drawLightAurora(w, h, dx, dy)
    }
}

private data class Blob(val center: Offset, val color: Color, val radius: Float)

private fun DrawScope.blobs(list: List<Blob>) {
    list.forEach { (center, color, radius) ->
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(color, Color.Transparent),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}

private fun DrawScope.drawLightAurora(w: Float, h: Float, dx: Float, dy: Float) {
    blobs(
        listOf(
            Blob(Offset(w * 0.16f + dx, h * 0.12f + dy), AuroraPinkLight.copy(alpha = .32f),   w * .60f),
            Blob(Offset(w * 0.86f + dx, h * 0.08f + dy), AuroraBlueLight.copy(alpha = .34f),   w * .65f),
            Blob(Offset(w * 0.84f + dx, h * 0.82f + dy), AuroraGreenLight.copy(alpha = .30f),  w * .70f),
            Blob(Offset(w * 0.08f + dx, h * 0.86f + dy), AuroraYellowLight.copy(alpha = .28f), w * .68f),
        ),
    )
}

private fun DrawScope.drawDarkAurora(w: Float, h: Float, dx: Float, dy: Float) {
    drawRect(color = BgDark)
    blobs(
        listOf(
            Blob(Offset(w * 0.14f + dx, h * 0.10f + dy), AuroraPurpleDark.copy(alpha = .26f), w * .62f),
            Blob(Offset(w * 0.88f + dx, h * 0.06f + dy), AuroraBlueDark.copy(alpha = .24f),  w * .65f),
            Blob(Offset(w * 0.86f + dx, h * 0.84f + dy), AuroraTealDark.copy(alpha = .20f),  w * .75f),
            Blob(Offset(w * 0.06f + dx, h * 0.90f + dy), AuroraPinkDark.copy(alpha = .18f),  w * .70f),
        ),
    )
}
