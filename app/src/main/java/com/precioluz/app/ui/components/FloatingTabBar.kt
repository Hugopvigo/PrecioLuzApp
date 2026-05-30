package com.precioluz.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Tab bar flotante con píldora deslizante — equivale al TabBar del HTML
@Composable
fun FloatingTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val pillColor = if (isDark) Color(0x2EFFFFFF) else Color(0xEAFFFFFF)

    GlassCard(
        modifier     = modifier.width(212.dp),
        cornerRadius = 999.dp,
        isDark       = isDark,
    ) {
        BoxWithConstraints(modifier = Modifier.padding(5.dp)) {
            val halfWidth = maxWidth / 2

            // Píldora deslizante animada
            val pillOffset by animateDpAsState(
                targetValue   = if (selectedTab == 0) 0.dp else halfWidth,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                label         = "tab_pill",
            )
            Box(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(halfWidth)
                    .height(42.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(pillColor),
            )

            // Tabs sobre la píldora
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabItem(
                    label    = "Hoy",
                    icon     = Icons.Rounded.LightMode,
                    selected = selectedTab == 0,
                    onClick  = { onTabSelected(0) },
                    modifier = Modifier.weight(1f),
                )
                TabItem(
                    label    = "Mañana",
                    icon     = Icons.Rounded.NightsStay,
                    selected = selectedTab == 1,
                    onClick  = { onTabSelected(1) },
                    modifier = Modifier.weight(1f),
                )
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
) {
    val contentColor = if (selected)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)

    Box(
        modifier         = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                modifier           = Modifier.size(18.dp),
                tint               = contentColor,
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor)
        }
    }
}
