package com.precioluz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Glassmorphism card: fondo semitransparente + borde sutil
// Sin blur nativo para no afectar al texto contenido
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    isDark: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val bgColor = if (isDark) Color(0xCC1A1033) else Color(0xAAFDFBFF)
    val borderColor = if (isDark) Color(0x44FFFFFF) else Color(0xBBFFFFFF)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = shape),
        content = content,
    )
}
