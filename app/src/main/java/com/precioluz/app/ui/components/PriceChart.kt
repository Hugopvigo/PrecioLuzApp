package com.precioluz.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.domain.model.Tramo
import com.precioluz.app.domain.model.formatPrice
import java.time.LocalTime

// Gráfico de 24 barras: se toca o se arrastra para inspeccionar cada hora.
@Composable
fun PriceChart(
    day: DayPrices,
    isToday: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentHour = LocalTime.now().hour.takeIf { isToday }
    var selected by rememberSaveable { mutableIntStateOf(currentHour ?: day.min.hour) }
    val maxPrice = day.max.price.coerceAtLeast(0.0001)
    val sur   = MaterialTheme.colorScheme.onSurface
    val nowBg = if (isDark) Color(0xFF4A3108) else Color(0xFFFFDDAA)
    val sel   = day.hours[selected]
    val selColor = if (isDark) sel.tier.colorDark() else sel.tier.colorLight()
    val chartHeight = 140.dp

    GlassCard(modifier = modifier.fillMaxWidth(), isDark = isDark) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
            Text(
                text  = "Precio por horas",
                style = MaterialTheme.typography.titleMedium,
                color = sur,
            )
            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(day) {
                            detectTapGestures { offset ->
                                selected = (offset.x / (size.width / 24f)).toInt().coerceIn(0, 23)
                            }
                        }
                        .pointerInput(day) {
                            detectHorizontalDragGestures { change, _ ->
                                selected = (change.position.x / (size.width / 24f)).toInt().coerceIn(0, 23)
                            }
                        },
                ) {
                    val barW  = size.width / 24f
                    val gap   = barW * 0.28f
                    val fillW = (barW - gap).coerceAtLeast(3f)
                    val baseline = size.height

                    // Franja más barata de 2 h: fondo discreto.
                    val best = day.bestWindow
                    drawRoundRect(
                        color = (if (isDark) colorTramo(Tramo.VALLE, true) else colorTramo(Tramo.VALLE, false))
                            .copy(alpha = .10f),
                        topLeft = Offset(best.first * barW, 0f),
                        size = Size(barW * best.count(), baseline),
                        cornerRadius = CornerRadius(6f, 6f),
                    )

                    // Columna de la hora actual.
                    currentHour?.let { h ->
                        drawRect(
                            color = nowBg.copy(alpha = if (isDark) .45f else .55f),
                            topLeft = Offset(h * barW, 0f),
                            size = Size(barW, baseline),
                        )
                    }

                    day.hours.forEach { h ->
                        val pct   = 0.10f + (h.price / maxPrice).toFloat() * 0.90f
                        val barH  = baseline * pct
                        val x     = h.hour * barW + gap / 2f
                        val y     = baseline - barH
                        val color = if (isDark) h.tier.colorDark() else h.tier.colorLight()

                        if (h.hour == selected) {
                            drawRoundRect(
                                color = color.copy(alpha = .20f),
                                topLeft = Offset(x - 4f, y - 6f),
                                size = Size(fillW + 8f, barH + 6f),
                                cornerRadius = CornerRadius(6f, 6f),
                            )
                        }
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = Size(fillW, barH),
                            cornerRadius = CornerRadius(3f, 3f),
                        )
                    }

                    // Línea de media discontinua.
                    val avgY = baseline - (day.avg / maxPrice).toFloat() * baseline
                    drawLine(
                        color = sur.copy(alpha = .38f),
                        start = Offset(0f, avgY),
                        end = Offset(size.width, avgY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                    )
                }

                // Etiqueta de la media, sobre la línea.
                Text(
                    text  = "media ${formatPrice(day.avg)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    color = sur.copy(alpha = .66f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = chartHeight * (1f - (day.avg / maxPrice).toFloat()) - 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isDark) Color(0xFF151C29) else Color(0xFFFFFFFF))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }

            Spacer(Modifier.height(6.dp))

            // Franja de tramos: valle verde, llano ámbar, punta rojo.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp)) {
                day.hours.forEach { h ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colorTramo(h.tramo, isDark)),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                listOf("00", "06", "12", "18", "23").forEach { h ->
                    Text(h, style = MaterialTheme.typography.labelMedium, color = sur.copy(alpha = .50f))
                }
            }

            Spacer(Modifier.height(10.dp))
            val whenText = when {
                currentHour == null -> ""
                selected == currentHour -> " · ahora"
                selected < currentHour -> " · ya pasó"
                else -> ""
            }
            Text(
                text = "${hour(sel.hour)}–${hour(sel.hour + 1)} h · ${formatPrice(sel.price)} €/kWh · " +
                    "${sel.tier.label()} · ${sel.tramo.label()}$whenText",
                style = MaterialTheme.typography.labelMedium,
                color = sur.copy(alpha = .86f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(sur.copy(alpha = .06f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                Tramo.entries.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(colorTramo(t, isDark)))
                        Spacer(Modifier.size(5.dp))
                        Text(t.label(), style = MaterialTheme.typography.labelMedium, color = sur.copy(alpha = .60f))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = "Mínima ${formatPrice(day.min.price)} a las ${hour(day.min.hour)} h · " +
                    "Máxima ${formatPrice(day.max.price)} a las ${hour(day.max.hour)} h",
                style = MaterialTheme.typography.labelMedium,
                color = sur.copy(alpha = .60f),
            )
        }
    }
}

private fun colorTramo(tramo: Tramo, isDark: Boolean): Color =
    if (isDark) tramo.colorDark() else tramo.colorLight()

private fun hour(h: Int): String = (h % 24).toString().padStart(2, '0')
