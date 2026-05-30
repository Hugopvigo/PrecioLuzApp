package com.precioluz.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.DayPrices

// Fila de 3 tarjetas: Mínima 💰 / Media / Máxima 💀
@Composable
fun StatRow(
    day: DayPrices,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(
            label    = "Mínima 💰",
            price    = day.min.price,
            subLabel = "${day.min.hour.toString().padStart(2,'0')}–${(day.min.hour+1).toString().padStart(2,'0')}h",
            color    = if (isDark) day.hours[day.min.hour].tier.colorDark()
                       else       day.hours[day.min.hour].tier.colorLight(),
            isDark   = isDark,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label    = "Media",
            price    = day.avg,
            subLabel = "€/kWh",
            color    = MaterialTheme.colorScheme.onSurface,
            isDark   = isDark,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label    = "Máxima 💀",
            price    = day.max.price,
            subLabel = "${day.max.hour.toString().padStart(2,'0')}–${(day.max.hour+1).toString().padStart(2,'0')}h",
            color    = if (isDark) day.hours[day.max.hour].tier.colorDark()
                       else       day.hours[day.max.hour].tier.colorLight(),
            isDark   = isDark,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    price: Double,
    subLabel: String,
    color: androidx.compose.ui.graphics.Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, isDark = isDark) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = .44f))
            Spacer(Modifier.height(5.dp))
            Text(
                text  = "%.3f".format(price).replace('.', ','),
                style = MaterialTheme.typography.headlineSmall,
                color = color,
            )
            Text(subLabel, style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = .44f))
        }
    }
}
