package es.hugopvigo.precioluz.ui.components

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Reemplaza el .glass de CSS con backdropFilter: blur()
// API 31+ (Android 12) usa RenderEffect nativo; fallback con alpha en API < 31
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp   = 26.dp,
    isDark: Boolean    = false,
    blurRadius: Float  = 28f,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape       = RoundedCornerShape(cornerRadius)
    val bgColor     = if (isDark) Color(0x662A1E4C) else Color(0x55FFFFFF)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0xD9FFFFFF)

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Android 12+ — blur nativo sobre lo que hay debajo
                    Modifier.graphicsLayer {
                        renderEffect = android.graphics.RenderEffect
                            .createBlurEffect(blurRadius, blurRadius, android.graphics.Shader.TileMode.CLAMP)
                            .asComposeRenderEffect()
                    }
                } else {
                    // Fallback: sin blur, solo fondo semitransparente
                    Modifier
                }
            )
            .drawBehind { drawRect(color = bgColor) }
            .border(width = 1.dp, color = borderColor, shape = shape),
        content = content,
    )
}
