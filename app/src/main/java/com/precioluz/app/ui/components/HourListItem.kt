package com.precioluz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.DayPrices
import java.time.LocalTime

@Composable
fun HourList(
    day: DayPrices,
    isToday: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentHour = if (isToday) LocalTime.now().hour else -1
    val maxPrice    = day.max.price

    // Top 3 más baratas y top 3 más caras
    val cheapest3 = day.hours.sortedBy { it.price }.take(3).map { it.hour }.toSet()
    val dearest3  = day.hours.sortedByDescending { it.price }.take(3).map { it.hour }.toSet()

    GlassCard(modifier = modifier.fillMaxWidth(), isDark = isDark) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text  = "Todas las horas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            PriceLegend(isDark = isDark)
            Spacer(Modifier.height(4.dp))

            day.hours.forEach { h ->
                val tierColor = if (isDark) h.tier.colorDark() else h.tier.colorLight()
                val isNow     = h.hour == currentHour
                val pct       = (h.price / maxPrice).toFloat()

                val mark = when {
                    h.hour in cheapest3 -> "💰"
                    h.hour in dearest3  -> "💀"
                    isNow               -> "·"
                    else                -> ""
                }
                val priceColor = when {
                    h.hour in cheapest3 || h.hour in dearest3 -> tierColor
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isNow) Modifier.background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f),
                                shape = RoundedCornerShape(12.dp),
                            ) else Modifier
                        )
                        .padding(horizontal = if (isNow) 8.dp else 0.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Hora (formato corto — "00h" en vez de "00–01h" para ganar espacio)
                    Text(
                        text  = "${h.hour.toString().padStart(2, '0')}h",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(38.dp),
                    )

                    // Tramo con punto de color
                    Row(
                        modifier          = Modifier.width(66.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(tierColor))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = h.tramo.label(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f),
                        )
                    }

                    // Barra de progreso
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .06f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(pct)
                                .clip(RoundedCornerShape(999.dp))
                                .background(tierColor),
                        )
                    }

                    // Emoji — 💰 las 3 más baratas, 💀 las 3 más caras
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text     = mark,
                        modifier = Modifier.width(22.dp),
                    )

                    // Precio con €
                    Text(
                        text     = "${"%.3f".format(h.price).replace('.', ',')} €",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = priceColor,
                        modifier = Modifier.width(62.dp),
                    )
                }

                if (h.hour < 23) {
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f),
                        thickness = .5.dp,
                    )
                }
            }
        }
    }
}

@Composable
fun PriceLegend(isDark: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        com.precioluz.app.domain.model.PriceTier.entries.forEach { tier ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = if (isDark) tier.colorDark() else tier.colorLight()
                Box(Modifier.size(7.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(5.dp))
                Text(
                    text  = tier.label(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f),
                )
            }
        }
    }
}
