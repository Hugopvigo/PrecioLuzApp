package com.precioluz.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.ui.components.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private val esDateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))

@Composable
fun TomorrowScreen(
    day: DayPrices?,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    if (day == null) {
        NotReadyYet(modifier)
        return
    }

    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Mañana", style = MaterialTheme.typography.displayMedium)
                    Text(
                        text  = day.date.format(esDateFormatter)
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
                    )
                }
                Text(
                    text  = "Previsión",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
                )
            }
        }

        item { HeroPriceCard(day = day, isToday = false, isDark = isDark) }
        item { StatRow(day = day, isDark = isDark) }
        item { RecoCard(day = day, isDark = isDark) }
        item { PriceChart(day = day, isToday = false, isDark = isDark) }
        item { HourList(day = day, isToday = false, isDark = isDark) }

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
private fun NotReadyYet(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⏳", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(12.dp))
            Text(
                text  = "Los precios de mañana se publican\naproximadamente a las 20:15",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f),
            )
        }
    }
}
