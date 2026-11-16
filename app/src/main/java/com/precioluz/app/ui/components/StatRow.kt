package com.precioluz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.domain.model.Tramo
import com.precioluz.app.domain.model.formatPrice

// Mínima / media / máxima del día. Se mantienen en las dos pestañas, como pediste.
@Composable
fun StatRow(
    day: DayPrices,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(9.dp)) {
        StatCard(
            label    = "Mínima",
            price    = day.min.price,
            subLabel = hourRange(day.min.hour),
            dotColor = tramoColor(Tramo.VALLE, isDark),
            isDark   = isDark,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label    = "Media",
            price    = day.avg,
            subLabel = "€/kWh",
            dotColor = null,
            isDark   = isDark,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label    = "Máxima",
            price    = day.max.price,
            subLabel = hourRange(day.max.hour),
            dotColor = tramoColor(Tramo.PUNTA, isDark),
            isDark   = isDark,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun tramoColor(tramo: Tramo, isDark: Boolean): Color =
    if (isDark) tramo.colorDark() else tramo.colorLight()

private fun hourRange(h: Int) = "${hour(h)}–${hour(h + 1)}h"

private fun hour(h: Int): String = (h % 24).toString().padStart(2, '0')

@Composable
private fun StatCard(
    label: String,
    price: Double,
    subLabel: String,
    dotColor: Color?,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, isDark = isDark) {
        Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (dotColor != null) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
                    Spacer(Modifier.size(5.dp))
                }
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text  = formatPrice(price),
                style = MaterialTheme.typography.headlineSmall,
                color = dotColor ?: MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .50f),
            )
        }
    }
}
