package com.precioluz.app.ui.home

import com.precioluz.app.domain.model.PriceDay
import com.precioluz.app.domain.model.PriceHour

data class HomeUiState(
    val todayPrices: PriceDay? = null,
    val tomorrowPrices: PriceDay? = null,
    val selectedTab: PriceTab = PriceTab.TODAY,
    val selectedHour: PriceHour? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class PriceTab {
    TODAY,
    TOMORROW
}
