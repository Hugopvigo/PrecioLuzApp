package com.precioluz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.precioluz.app.ui.theme.GlassBgDark
import com.precioluz.app.ui.theme.GlassBgLight
import com.precioluz.app.ui.theme.GlassBorderDark
import com.precioluz.app.ui.theme.GlassBorderLight

// Superficie sólida con borde de 1 px, como las cards de la web.
// Se conserva el nombre GlassCard para no romper las pantallas existentes.
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    isDark: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val bgColor = if (isDark) GlassBgDark else GlassBgLight
    val borderColor = if (isDark) GlassBorderDark else GlassBorderLight

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = shape),
        content = content,
    )
}
