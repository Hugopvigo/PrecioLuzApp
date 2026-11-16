package com.precioluz.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    uiState: SettingsUiState,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onNotifyTomorrowChanged: (Boolean) -> Unit,
    onNotifyDailyChanged: (Boolean) -> Unit,
    onEnableNotifications: () -> Unit,
) {
    val surface = if (isDark) Color(0xFF151C29) else Color(0xFFFFFFFF)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = surface) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text("Ajustes", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                "Los avisos se programan en este dispositivo, sin cuenta ni registro.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f),
            )

            Spacer(Modifier.height(20.dp))
            SwitchRow(
                icon = Icons.Rounded.CalendarToday,
                label = "Precios de mañana",
                description = "Aviso cuando se publican (~20:15 h)",
                checked = uiState.notifyTomorrow,
                isDark = isDark,
                onCheckedChange = { enabled ->
                    onNotifyTomorrowChanged(enabled)
                    if (enabled) onEnableNotifications()
                },
            )
            SwitchRow(
                icon = Icons.Rounded.Schedule,
                label = "Resumen del día",
                description = "Mínima, media y máxima a las 08:00 h",
                checked = uiState.notifyDaily,
                isDark = isDark,
                onCheckedChange = { enabled ->
                    onNotifyDailyChanged(enabled)
                    if (enabled) onEnableNotifications()
                },
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "PrecioLuz 1.1.0 · datos PVPC de Redeia (REE)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .44f),
            )
        }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    isDark: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier         = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .07f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f),
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isDark) Color(0xFF0E1420) else Color(0xFFFFFFFF),
                checkedTrackColor = Color(0xFF05994A),
            ),
        )
    }
}
