package com.precioluz.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.precioluz.app.data.datastore.AppTheme
import com.precioluz.app.data.datastore.SettingsDataStore
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.domain.usecase.GetTodayPricesUseCase
import com.precioluz.app.domain.usecase.GetTomorrowPricesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PrecioLuzVM"

private fun mensajeError(e: Throwable): String {
    val msg = e.message ?: ""
    return when {
        msg.contains("NO_DATA")                     -> "No hay datos disponibles para esta fecha"
        msg.contains("HTTP 404")                    -> "Datos no disponibles"
        msg.contains("HTTP 5")                      -> "Error en el servidor. Inténtalo más tarde"
        msg.contains("UnknownHostException") ||
        msg.contains("Unable to resolve host") ||
        msg.contains("NetworkException")            -> "Sin conexión a internet"
        msg.contains("timeout", ignoreCase = true) ||
        msg.contains("SocketTimeout")              -> "Tiempo de espera agotado. Comprueba tu conexión"
        else                                        -> "No se pudieron cargar los precios. Inténtalo de nuevo"
    }
}

data class PrecioLuzUiState(
    val today: DayPrices?          = null,
    val tomorrow: DayPrices?       = null,
    val isLoading: Boolean         = true,
    val error: String?             = null,
    val tomorrowNotReady: Boolean  = false,
)

@HiltViewModel
class PrecioLuzViewModel @Inject constructor(
    private val getTodayPrices: GetTodayPricesUseCase,
    private val getTomorrowPrices: GetTomorrowPricesUseCase,
    private val settings: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrecioLuzUiState())
    val uiState: StateFlow<PrecioLuzUiState> = _uiState.asStateFlow()

    val theme: StateFlow<AppTheme> = settings.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.AUTO)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                getTodayPrices().collect { result ->
                    result
                        .onSuccess { day -> _uiState.update { it.copy(today = day) } }
                        .onFailure { e  -> _uiState.update { it.copy(error = mensajeError(e)) } }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = mensajeError(e)) }
            }

            try {
                getTomorrowPrices().collect { result ->
                    result
                        .onSuccess { day ->
                            _uiState.update { it.copy(
                                tomorrow = day,
                                tomorrowNotReady = day == null,
                                isLoading = false,
                            )}
                        }
                        .onFailure { _uiState.update { it.copy(isLoading = false) } }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun cycleTheme() {
        viewModelScope.launch {
            settings.setTheme(when (theme.value) {
                AppTheme.AUTO  -> AppTheme.LIGHT
                AppTheme.LIGHT -> AppTheme.DARK
                AppTheme.DARK  -> AppTheme.AUTO
            })
        }
    }
}
