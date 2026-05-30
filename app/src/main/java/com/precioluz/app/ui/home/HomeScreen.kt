package com.precioluz.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.PriceCategory
import com.precioluz.app.domain.model.PriceDay
import com.precioluz.app.domain.model.PriceHour
import com.precioluz.app.domain.model.category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTabSelected: (PriceTab) -> Unit,
    onHourSelected: (PriceHour?) -> Unit,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PrecioLuz") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Ajustes"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) { Text("Reintentar") }
                    }
                }
            }
            else -> {
                val prices = when (uiState.selectedTab) {
                    PriceTab.TODAY -> uiState.todayPrices
                    PriceTab.TOMORROW -> uiState.tomorrowPrices
                }
                Column(modifier = Modifier.padding(padding)) {
                    PriceTabs(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = onTabSelected,
                        tomorrowAvailable = uiState.tomorrowPrices != null
                    )
                    if (prices != null) {
                        PriceSummaryCard(prices)
                        PriceChartList(
                            prices = prices,
                            onHourSelected = onHourSelected,
                            darkTheme = isSystemInDarkTheme()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Los precios de mañana aún no están publicados.\nPrueba después de las 20:30",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceTabs(
    selectedTab: PriceTab,
    onTabSelected: (PriceTab) -> Unit,
    tomorrowAvailable: Boolean
) {
    TabRow(selectedTabIndex = selectedTab.ordinal) {
        Tab(
            selected = selectedTab == PriceTab.TODAY,
            onClick = { onTabSelected(PriceTab.TODAY) },
            text = { Text("Hoy") }
        )
        Tab(
            selected = selectedTab == PriceTab.TOMORROW,
            onClick = { onTabSelected(PriceTab.TOMORROW) },
            text = {
                Text(
                    "Mañana",
                    color = if (tomorrowAvailable)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        )
    }
}

@Composable
private fun PriceSummaryCard(day: PriceDay) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            day.min?.let { cheapest ->
                SummaryItem(
                    label = "Más barata",
                    value = "${cheapest.hour}:00h",
                    price = cheapest.priceKwh,
                    color = categoryColor(PriceCategory.CHEAP, isSystemInDarkTheme())
                )
            }
            day.max?.let { dearest ->
                SummaryItem(
                    label = "Más cara",
                    value = "${dearest.hour}:00h",
                    price = dearest.priceKwh,
                    color = categoryColor(PriceCategory.DEAR, isSystemInDarkTheme())
                )
            }
            SummaryItem(
                label = "Media",
                value = "",
                price = day.average,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, price: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        if (value.isNotEmpty()) {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Text(
            formatPrice(price),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun PriceChartList(prices: PriceDay, onHourSelected: (PriceHour?) -> Unit, darkTheme: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(prices.prices) { priceHour ->
            val cat   = priceHour.category(prices)
            val color = categoryColor(cat, darkTheme)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHourSelected(priceHour) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = "${priceHour.hour.toString().padStart(2, '0')}h",
                    style    = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(40.dp),
                )
                LinearProgressIndicator(
                    progress = {
                        val maxPrice = prices.max?.priceKwh ?: 1.0
                        (priceHour.priceKwh / maxPrice).toFloat().coerceIn(0f, 1f)
                    },
                    color    = color,
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .padding(horizontal = 8.dp),
                )
                Text(
                    text      = formatPrice(priceHour.priceKwh),
                    style     = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier  = Modifier.width(80.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }
        }
    }
}

private fun categoryColor(cat: PriceCategory, dark: Boolean): Color = when (cat) {
    PriceCategory.CHEAP      -> if (dark) Color(0xFF30D158) else Color(0xFF15A34A)
    PriceCategory.AFFORDABLE -> if (dark) Color(0xFFFFD60A) else Color(0xFFC28A00)
    PriceCategory.MEDIUM     -> if (dark) Color(0xFFFF9F0A) else Color(0xFFE26A07)
    PriceCategory.DEAR       -> if (dark) Color(0xFFFF6961) else Color(0xFFE11D2E)
}

private fun formatPrice(priceKwh: Double): String =
    String.format("%.4f", priceKwh).replace(".", ",") + " €/kWh"
