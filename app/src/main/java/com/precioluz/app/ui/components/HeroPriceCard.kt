package com.precioluz.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.domain.model.PriceTier
import com.precioluz.app.domain.model.Tramo
import com.precioluz.app.domain.model.formatPrice
import com.precioluz.app.domain.model.versusAverage
import java.time.LocalTime

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
    val tier        = hourData?.tier ?: day.hours[day.min.hour].tier
    val tierColor   = if (isDark) tier.colorDark() else tier.colorLight()

    GlassCard(modifier = modifier.fillMaxWidth(), isDark = isDark) {
        // Halo tenue del color del tramo, solo en la esquina superior derecha.
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(tierColor.copy(alpha = .16f), Color.Transparent),
                    center = Offset(size.width * 0.9f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(
                text = if (isToday)
                    "Ahora · ${hour(currentHour)}–${hour(currentHour + 1)} h · ${hourData?.tramo?.label().orEmpty()}"
                else
                    "Precio medio · mañana",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f),
            )

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text  = formatPrice(price),
                    style = MaterialTheme.typography.displayLarge,
                    color = tierColor,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text     = "€/kWh",
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f),
                    modifier = Modifier.padding(bottom = 7.dp),
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isToday) versusAverage(price, day.avg)
                       else "Mín ${formatPrice(day.min.price)} · Máx ${formatPrice(day.max.price)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f),
            )

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                PriceTierChip(tier = tier, isDark = isDark, estimated = !isToday)
                hourData?.let { TramoChip(tramo = it.tramo) }
            }
            if (isToday && hourData != null) {
                Spacer(Modifier.height(8.dp))
                TrendChip(day = day, currentHour = currentHour, isDark = isDark)
            }
        }
    }
}

private fun hour(h: Int): String = (h % 24).toString().padStart(2, '0')

@Composable
fun PriceTierChip(tier: PriceTier, isDark: Boolean, estimated: Boolean = false) {
    val color = if (isDark) tier.colorDark() else tier.colorLight()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = .16f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Text(
            text  = if (estimated) "estimado" else tier.label(),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            color = color,
        )
    }
}

@Composable
fun TramoChip(tramo: Tramo) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Text(
        text  = tramo.label(),
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
        color = onSurface.copy(alpha = .72f),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(onSurface.copy(alpha = .06f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
fun TrendChip(day: DayPrices, currentHour: Int, isDark: Boolean) {
    val nextHour  = (currentHour + 1) % 24
    val up        = day.hours[nextHour].price >= day.hours[currentHour].price
    val trendTier = if (up) PriceTier.RED else PriceTier.GREEN
    val color     = if (isDark) trendTier.colorDark() else trendTier.colorLight()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text  = if (up) "↑" else "↓",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
            color = color,
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text  = "${if (up) "Sube" else "Baja"} a las ${hour(nextHour)}:00",
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}
