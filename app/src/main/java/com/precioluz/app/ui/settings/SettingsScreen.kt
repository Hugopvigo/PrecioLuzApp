package com.precioluz.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onNotifyTomorrowChanged: (Boolean) -> Unit,
    onNotifyDailyChanged: (Boolean) -> Unit,
    onDarkThemeChanged: (DarkThemePreference) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            NotificationSection(
                notifyTomorrow = uiState.notifyTomorrow,
                notifyDaily = uiState.notifyDaily,
                onNotifyTomorrowChanged = onNotifyTomorrowChanged,
                onNotifyDailyChanged = onNotifyDailyChanged
            )

            Spacer(modifier = Modifier.height(24.dp))

            ThemeSection(
                selected = uiState.darkTheme,
                onSelected = onDarkThemeChanged
            )
        }
    }
}

@Composable
private fun NotificationSection(
    notifyTomorrow: Boolean,
    notifyDaily: Boolean,
    onNotifyTomorrowChanged: (Boolean) -> Unit,
    onNotifyDailyChanged: (Boolean) -> Unit
) {
    Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))

    SwitchRow(
        label = "Precios de mañana",
        description = "Notificación cuando se publiquen los precios del día siguiente (~20:30)",
        checked = notifyTomorrow,
        onCheckedChange = onNotifyTomorrowChanged
    )

    SwitchRow(
        label = "Resumen del día",
        description = "Resumen diario con la hora más barata (8:00)",
        checked = notifyDaily,
        onCheckedChange = onNotifyDailyChanged
    )
}

@Composable
private fun SwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThemeSection(
    selected: DarkThemePreference,
    onSelected: (DarkThemePreference) -> Unit
) {
    Text("Tema", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))

    Column(modifier = Modifier.selectableGroup()) {
        DarkThemePreference.entries.forEach { preference ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected == preference,
                        onClick = { onSelected(preference) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected == preference, onClick = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when (preference) {
                        DarkThemePreference.SYSTEM -> "Sistema"
                        DarkThemePreference.LIGHT -> "Claro"
                        DarkThemePreference.DARK -> "Oscuro"
                    }
                )
            }
        }
    }
}
