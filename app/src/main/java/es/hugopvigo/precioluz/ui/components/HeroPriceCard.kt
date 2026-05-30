package es.hugopvigo.precioluz.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.hugopvigo.precioluz.domain.model.DayPrices
import es.hugopvigo.precioluz.domain.model.PriceTier
import java.time.LocalTime

// Hero card — precio grande coloreado por tier
// Equivale al componente Hero del diseño HTML
@Composable
fun HeroPriceCard(
    day: DayPrices,
    isToday: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentHour = LocalTime.now().hour
    val hourData    = if (isToday) day.hours[currentHour] else null
    val price       = hourData?.price ?: day.avg
    val tier        = hourData?.tier  ?: day.hours[day.min.hour].tier
    val tierColor   = if (isDark) tier.colorDark() else tier.colorLight()

    GlassCard(modifier = modifier.fillMaxWidth(), isDark = isDark) {
        // Glow de fondo con el color del tier
        // TODO: drawBehind radialGradient desde esquina superior derecha

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            // Label
            val label = if (isToday)
                "Ahora · ${currentHour.toString().padStart(2,'0')}–${(currentHour+1).toString().padStart(2,'0')}h"
            else
                "Precio medio · mañana"
            Text(text = label, style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f))

            // Precio grande
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text  = "%.3f".format(price).replace('.', ','),
                    style = MaterialTheme.typography.displayLarge,
                    color = tierColor,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text     = "€/kWh",
                    style    = MaterialTheme.typography.titleLarge,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            // Tags: tier + tramo + tendencia
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriceTierChip(tier = tier, isDark = isDark)
                if (isToday && hourData != null) {
                    TramoChip(tramo = hourData.tramo)
                    TrendChip(day = day, currentHour = currentHour, isDark = isDark)
                }
            }
        }
    }
}

// Chips auxiliares — TODO: implementar en chips separados o inline
@Composable fun PriceTierChip(tier: PriceTier, isDark: Boolean) { /* TODO */ }
@Composable fun TramoChip(tramo: es.hugopvigo.precioluz.domain.model.Tramo) { /* TODO */ }
@Composable fun TrendChip(day: DayPrices, currentHour: Int, isDark: Boolean) { /* TODO */ }
