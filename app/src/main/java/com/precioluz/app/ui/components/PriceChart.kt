package com.precioluz.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.DayPrices
import java.time.LocalTime

// Gráfico de 24 barras interactivo — toca una barra para ver el precio
@Composable
fun PriceChart(
    day: DayPrices,
    isToday: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentHour = if (isToday) LocalTime.now().hour else -1
    var selectedHour by remember { mutableIntStateOf(if (isToday) currentHour else day.min.hour) }
    val maxPrice = day.max.price

    GlassCard(modifier = modifier.fillMaxWidth(), isDark = isDark) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("24 horas", style = MaterialTheme.typography.titleLarge)
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    val selH      = day.hours[selectedHour]
                    val tierColor = if (isDark) selH.tier.colorDark() else selH.tier.colorLight()
                    Text(
                        text  = "${selectedHour.toString().padStart(2, '0')}–${(selectedHour + 1).toString().padStart(2, '0')}h",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .44f),
                    )
                    Text(
                        text  = "%.3f €/kWh".format(selH.price).replace('.', ','),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tierColor,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .pointerInput(day) {
                        detectTapGestures { offset ->
                            val barW  = size.width / 24f
                            val tapped = (offset.x / barW).toInt().coerceIn(0, 23)
                            selectedHour = tapped
                        }
                    },
            ) {
                val barW  = size.width / 24f
                val gap   = barW * 0.25f
                val fillW = (barW - gap).coerceAtLeast(4f)

                day.hours.forEach { h ->
                    val heightPct = 0.12f + (h.price / maxPrice).toFloat() * 0.88f
                    val barH      = size.height * heightPct
                    val x         = h.hour * barW + gap / 2f
                    val y         = size.height - barH
                    val color     = if (isDark) h.tier.colorDark() else h.tier.colorLight()
                    val isSel     = h.hour == selectedHour

                    if (isSel) {
                        // Glow exterior: dos rects concéntricos con alpha reducida
                        drawRoundRect(
                            color        = color.copy(alpha = .18f),
                            topLeft      = Offset(x - 5f, y - 8f),
                            size         = Size(fillW + 10f, barH + 8f),
                            cornerRadius = CornerRadius(7f, 7f),
                        )
                        drawRoundRect(
                            color        = color.copy(alpha = .30f),
                            topLeft      = Offset(x - 2f, y - 4f),
                            size         = Size(fillW + 4f, barH + 4f),
                            cornerRadius = CornerRadius(5f, 5f),
                        )
                    }

                    drawRoundRect(
                        color        = if (isSel) color else color.copy(alpha = .88f),
                        topLeft      = Offset(x, y),
                        size         = Size(fillW, barH),
                        cornerRadius = CornerRadius(4f, 4f),
                    )

                    // Punto de la hora actual
                    if (h.hour == currentHour) {
                        drawCircle(
                            color  = Color.White,
                            radius = 3f,
                            center = Offset(x + fillW / 2f, y - 6f),
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("00", "06", "12", "18", "23").forEach { h ->
                    Text(
                        text  = h,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .44f),
                    )
                }
            }
        }
    }
}
