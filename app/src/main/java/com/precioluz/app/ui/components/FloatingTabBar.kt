package com.precioluz.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp

// Tab bar flotante de cristal: Hoy / Mañana
@Composable
fun FloatingTabBar(
    selectedTab: Int,       // 0 = Hoy, 1 = Mañana
    onTabSelected: (Int) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier     = modifier.width(212.dp),
        cornerRadius = 999.dp,
        isDark       = isDark,
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
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

@Composable
private fun TabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)

    Button(
        onClick  = onClick,
        modifier = modifier.height(42.dp),
        shape    = RoundedCornerShape(999.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surface.copy(alpha = .9f)
                             else          androidx.compose.ui.graphics.Color.Transparent,
            contentColor   = contentColor,
        ),
        elevation = if (selected) ButtonDefaults.buttonElevation(defaultElevation = 4.dp) else null,
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
