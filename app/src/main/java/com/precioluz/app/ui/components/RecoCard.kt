package com.precioluz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.domain.model.Tramo
import com.precioluz.app.domain.model.cheapestWindow
import com.precioluz.app.domain.model.formatPrice
import com.precioluz.app.domain.model.savingPercent
import com.precioluz.app.domain.model.windowAverage
import java.time.LocalTime

private val DURATIONS = listOf(1, 2, 3)

// Recomendación de la franja más barata con duración configurable (1/2/3 h),
// más la hora más cara que la app mostraba antes: no se pierde información.
@Composable
fun RecoCard(
    day: DayPrices,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val duration = rememberSaveable { mutableIntStateOf(2) }
    val nowHour  = LocalTime.now().hour.takeIf { isToday(day) }

    val best = cheapestWindow(day.hours, nowHour ?: 0, duration.intValue)
    val green = tramoColor(Tramo.VALLE, isDark)
    val red   = tramoColor(Tramo.PUNTA, isDark)

    val (headline, detail) = recommendation(day, nowHour, duration.intValue, best)

    GlassCard(modifier = modifier.fillMaxWidth(), isDark = isDark) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text  = "¿Cuánto dura lo que vas a poner?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                DurationSelector(duration.intValue, { duration.intValue = it }, isDark)
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text  = headline,
                style = MaterialTheme.typography.bodyLarge,
                color = if (best != null) green else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = detail,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f),
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .10f))
            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(red.copy(alpha = .14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = red,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text  = "Hora más cara — evítala",
                        style = MaterialTheme.typography.labelMedium,
                        color = red,
                    )
                    Text(
                        text  = "${hour(day.max.hour)}–${hour(day.max.hour + 1)}h · ${formatPrice(day.max.price)} €/kWh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationSelector(selected: Int, onSelect: (Int) -> Unit, isDark: Boolean) {
    val bg        = if (isDark) Color(0xFF0A0E16) else Color(0xFFE9EEF7)
    val selectedBg = if (isDark) Color(0xFF2A3446) else Color(0xFFFFFFFF)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .5f), RoundedCornerShape(10.dp))
            .padding(2.dp),
    ) {
        DURATIONS.forEach { d ->
            val active = d == selected
            Text(
                text = "$d h",
                style = MaterialTheme.typography.labelMedium,
                color = if (active) {
                    if (isDark) Color(0xFFF1F4FA) else Color(0xFF0E1420)
                } else MaterialTheme.colorScheme.onSurface.copy(alpha = .60f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) selectedBg else Color.Transparent)
                    .clickable { onSelect(d) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

private fun recommendation(
    day: DayPrices,
    nowHour: Int?,
    duration: Int,
    best: IntRange?,
): Pair<String, String> {
    if (best == null) {
        return "Hoy ya no quedan $duration horas seguidas." to
            "Prueba con menos tiempo o mira los precios de mañana."
    }
    if (nowHour == null) {
        return "De ${hour(best.first)}:00 a ${hour(best.last + 1)}:00." to
            "Media ${formatPrice(windowAverage(day.hours, best))} €/kWh."
    }
    if (best.first == nowHour) {
        return "Ahora mismo." to "No queda una franja mejor hoy."
    }
    val end = minOf(nowHour + duration, day.hours.size)
    val nowAvg = if (nowHour < end) (nowHour until end).sumOf { day.hours[it].price } / (end - nowHour) else 0.0
    val saving = savingPercent(windowAverage(day.hours, best), nowAvg)
    val detail = "Media ${formatPrice(windowAverage(day.hours, best))} €/kWh" +
        if (saving > 0) ", un $saving % menos que empezando ahora." else "."
    return "Espera a las ${hour(best.first)}:00 (hasta las ${hour(best.last + 1)}:00)." to detail
}

private fun isToday(day: DayPrices) = day.date == java.time.LocalDate.now()

private fun tramoColor(tramo: Tramo, isDark: Boolean): Color =
    if (isDark) tramo.colorDark() else tramo.colorLight()

private fun hour(h: Int): String = (h % 24).toString().padStart(2, '0')
