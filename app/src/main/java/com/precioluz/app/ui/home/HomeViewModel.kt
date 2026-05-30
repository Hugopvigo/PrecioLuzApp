package com.precioluz.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.precioluz.app.domain.model.DateTarget
import com.precioluz.app.domain.usecase.GetPricesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPricesUseCase: GetPricesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPrices()
    }

    fun loadPrices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val (todayPrices, target) = getPricesUseCase.getToday()
                val tomorrowPrices = getPricesUseCase.getTomorrow()

                _uiState.value = _uiState.value.copy(
                    todayPrices = todayPrices,
                    tomorrowPrices = tomorrowPrices,
                    isLoading = false,
                    selectedTab = if (target.label == "mañana") PriceTab.TOMORROW else PriceTab.TODAY
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun selectTab(tab: PriceTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectHour(hour: PriceHour?) {
        _uiState.value = _uiState.value.copy(selectedHour = hour)
    }
}
