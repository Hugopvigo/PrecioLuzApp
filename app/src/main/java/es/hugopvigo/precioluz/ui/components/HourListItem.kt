package es.hugopvigo.precioluz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import es.hugopvigo.precioluz.domain.model.DayPrices
import es.hugopvigo.precioluz.ui.theme.LiveGreen
import java.time.LocalTime

// Lista completa de las 24 horas — equivale a HourList del diseño HTML
@Composable
fun HourList(
    day: DayPrices,
    isToday: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentHour = if (isToday) LocalTime.now().hour else -1
    val maxPrice    = day.max.price

    GlassCard(modifier = modifier.fillMaxWidth(), isDark = isDark) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Todas las horas", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            // Leyenda de colores
            PriceLegend(isDark = isDark)
            Spacer(Modifier.height(4.dp))

            day.hours.forEach { h ->
                val tierColor = if (isDark) h.tier.colorDark() else h.tier.colorLight()
                val isNow     = h.hour == currentHour
                val pct       = (h.price / maxPrice).toFloat()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isNow) Modifier.background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f),
                            shape = RoundedCornerShape(12.dp),
                        ) else Modifier)
                        .padding(horizontal = if (isNow) 8.dp else 0.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Franja horaria
                    Text(
                        text     = "${h.hour.toString().padStart(2,'0')}–${(h.hour+1).toString().padStart(2,'0')}h",
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(60.dp),
                    )

                    // Tramo
                    Row(modifier = Modifier.width(66.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(tierColor))
                        Spacer(Modifier.width(6.dp))
                        Text(h.tramo.label(), style = MaterialTheme.typography.labelMedium,
                             color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f))
                    }

                    // Barra de progreso
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .06f))
                    ) {
                        Box(Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(pct)
                            .clip(RoundedCornerShape(999.dp))
                            .background(tierColor))
                    }

                    // Icono especial
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = when { h.isMin -> "💰"; h.isMax -> "💀"; isNow -> "•"; else -> " " },
                        modifier = Modifier.width(18.dp),
                    )

                    // Precio
                    Text(
                        text  = "%.3f".format(h.price).replace('.', ','),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (h.isMin || h.isMax) tierColor else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(50.dp),
                    )
                }

                if (h.hour < 23) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f), thickness = .5.dp)
                }
            }
        }
    }
}

@Composable
fun PriceLegend(isDark: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        es.hugopvigo.precioluz.domain.model.PriceTier.entries.forEach { tier ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = if (isDark) tier.colorDark() else tier.colorLight()
                Box(Modifier.size(7.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(5.dp))
                Text(tier.label(), style = MaterialTheme.typography.labelMedium,
                     color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f))
            }
        }
    }
}
