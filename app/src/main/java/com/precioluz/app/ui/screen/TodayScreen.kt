package com.precioluz.app.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.ui.components.*
import com.precioluz.app.ui.theme.LiveGreen
import java.time.format.DateTimeFormatter
import java.util.Locale

private val esDateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))

@Composable
fun TodayScreen(
    day: DayPrices,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Hoy", style = MaterialTheme.typography.displayMedium)
                    Text(
                        text  = day.date.format(esDateFormatter)
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
                    )
                }
                LiveIndicator()
            }
        }

        item { HeroPriceCard(day = day, isToday = true, isDark = isDark) }
        item { StatRow(day = day, isDark = isDark) }
        item { RecoCard(day = day, isDark = isDark) }
        item { PriceChart(day = day, isToday = true, isDark = isDark) }
        item { HourList(day = day, isToday = true, isDark = isDark) }

        item {
            Text(
                text  = "Datos PVPC · ${day.date.format(DateTimeFormatter.ofPattern("d MMM", Locale("es","ES")))} · precios con impuestos incluidos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .44f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp),
            )
        }
    }
}

@Composable
fun LiveIndicator() {
    val infinite = rememberInfiniteTransition(label = "live_ping")
    val pingScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue  = 2.6f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1900, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ping_scale",
    )
    val pingAlpha by infinite.animateFloat(
        initialValue = 0.7f,
        targetValue  = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1900, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ping_alpha",
    )

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Dot con anillo de ping animado
        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotR = 3.5.dp.toPx()
                drawCircle(color = LiveGreen.copy(alpha = pingAlpha), radius = dotR * pingScale)
                drawCircle(color = LiveGreen, radius = dotR)
            }
        }
        Text(
            text  = "En directo",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f),
        )
    }
}
