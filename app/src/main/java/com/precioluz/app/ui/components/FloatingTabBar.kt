package com.precioluz.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// La píldora flotante de Hoy/Mañana se conserva: es un rasgo propio de la app
// que la web no tiene y Hugo quiere mantener.
@Composable
fun FloatingTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val container = if (isDark) Color(0xFF151C29) else Color(0xFFF7F9FC)
    val border    = if (isDark) Color(0xFF2D384D) else Color(0xFFC9D2E1)
    val pill      = if (isDark) Color(0xFF2A3446) else Color(0xFFFFFFFF)
    val shape     = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .width(206.dp)
            .clip(shape)
            .background(container)
            .border(1.dp, border, shape)
    ) {
        BoxWithConstraints(modifier = Modifier.padding(4.dp)) {
            val halfWidth = maxWidth / 2
            val pillOffset by animateDpAsState(
                targetValue   = if (selectedTab == 0) 0.dp else halfWidth,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label         = "tab_pill",
            )
            Box(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(halfWidth)
                    .height(40.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(pill),
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TabItem("Hoy", Icons.Rounded.LightMode, selectedTab == 0, { onTabSelected(0) }, Modifier.weight(1f), isDark)
                TabItem("Mañana", Icons.Rounded.NightsStay, selectedTab == 1, { onTabSelected(1) }, Modifier.weight(1f), isDark)
            }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean,
) {
    val color = when {
        selected && isDark -> Color(0xFFF1F4FA)
        selected           -> Color(0xFF0E1420)
        isDark             -> Color(0xFFA3AFC4)
        else               -> Color(0xFF4B5870)
    }
    Box(
        modifier         = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.width(17.dp), tint = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}
