package com.precioluz.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    val tier        = hourData?.tier  ?: day.hours[day.min.hour].tier
    val tierColor   = if (isDark) tier.colorDark() else tier.colorLight()

    GlassCard(modifier = modifier.fillMaxWidth(), isDark = isDark) {
        // Glow radial desde esquina superior-derecha, coloreado por tier
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(tierColor.copy(alpha = .38f), Color.Transparent),
                    center = Offset(size.width * 0.88f, 0f),
                    radius = size.width * 0.95f,
                ),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            val label = if (isToday)
                "Ahora · ${currentHour.toString().padStart(2, '0')}–${(currentHour + 1).toString().padStart(2, '0')}h"
            else
                "Precio medio · mañana"
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
            )

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

            Spacer(Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                PriceTierChip(tier = tier, isDark = isDark, estimated = !isToday)
                if (isToday && hourData != null) {
                    TramoChip(tramo = hourData.tramo)
                    TrendChip(day = day, currentHour = currentHour, isDark = isDark)
                } else if (!isToday) {
                    Text(
                        text  = "Mín ${"%.3f".format(day.min.price).replace('.', ',')} · Máx ${"%.3f".format(day.max.price).replace('.', ',')}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
                    )
                }
            }
        }
    }
}

@Composable
fun PriceTierChip(tier: PriceTier, isDark: Boolean, estimated: Boolean = false) {
    val color = if (isDark) tier.colorDark() else tier.colorLight()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = .18f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text  = if (estimated) "estimado" else tier.label(),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
            color = color,
        )
    }
}

@Composable
fun TramoChip(tramo: Tramo) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Text(
        text     = tramo.label(),
        style    = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
        color    = onSurface.copy(alpha = .66f),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(onSurface.copy(alpha = .06f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

@Composable
fun TrendChip(day: DayPrices, currentHour: Int, isDark: Boolean) {
    val nextHour  = (currentHour + 1) % 24
    val up        = day.hours[nextHour].price >= day.hours[currentHour].price
    val trendTier = if (up) PriceTier.RED else PriceTier.GREEN
    val color     = if (isDark) trendTier.colorDark() else trendTier.colorLight()

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text  = if (up) "↑" else "↓",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
            color = color,
        )
        Text(
            text  = "${if (up) "Sube" else "Baja"} a las ${nextHour.toString().padStart(2, '0')}:00",
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}
