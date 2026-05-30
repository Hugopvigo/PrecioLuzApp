package com.precioluz.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.ui.components.*

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
            // Cabecera
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Hoy", style = MaterialTheme.typography.displayMedium)
                    Text(
                        text  = day.date.toString(),   // TODO: formatear en español
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
                    )
                }
                // Indicador "En directo"
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
                text  = "Datos PVPC · ${day.date} · precios con impuestos incluidos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .44f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp),  // espacio para la tab bar flotante
            )
        }
    }
}

@Composable
fun LiveIndicator() {
    // TODO: punto verde animado (equivale a .live-dot con animación ping)
    Text("● En directo", style = MaterialTheme.typography.labelMedium,
         color = com.precioluz.app.ui.theme.LiveGreen)
}
