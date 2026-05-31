package com.precioluz.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SettingsDialog(
    isDark: Boolean,
    onDismiss: () -> Unit,
) {
    val dialogBg = if (isDark) Color(0xDD1A1033) else Color(0xDDFDFBFF)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = "Los precios se obtienen automáticamente sin necesidad de configuración.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
    )
}
