package com.precioluz.app.ui.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PrecioLuzVM"

private fun mensajeError(e: Throwable): String {
    val msg = e.message ?: ""
    return when {
        msg.contains("API_KEY_MISSING")             -> "API Key no configurada"
        msg.contains("NO_DATA")                     -> "No hay datos disponibles para esta fecha"
        msg.contains("HTTP 401")                    -> "API Key inválida o caducada"
        msg.contains("HTTP 403")                    -> "API Key sin permisos"
        msg.contains("HTTP 404")                    -> "Datos no disponibles"
        msg.contains("HTTP 5")                      -> "Error en el servidor de REE. Inténtalo más tarde"
        msg.contains("UnknownHostException") ||
        msg.contains("Unable to resolve host") ||
        msg.contains("NetworkException")            -> "Sin conexión a internet"
        msg.contains("timeout", ignoreCase = true) ||
        msg.contains("SocketTimeout")              -> "Tiempo de espera agotado. Comprueba tu conexión"
        else                                        -> "No se pudieron cargar los precios. Inténtalo de nuevo"
    }
}

data class PrecioLuzUiState(
    val today: DayPrices?        = null,
    val tomorrow: DayPrices?     = null,
    val isLoading: Boolean       = true,
    val error: String?           = null,
    val tomorrowNotReady: Boolean = false,
    val noApiKey: Boolean        = true,
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

    val apiKey: StateFlow<String?> = settings.apiKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            val key = settings.apiKey.first()
            Log.d(TAG, "init: apiKey=${if (key.isNullOrBlank()) "NULL" else "OK(${key.length}chars)"}")
            if (key.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        noApiKey = true,
                        isLoading = false,
                        error = "Configura tu API Key de REE en Ajustes",
                    )
                }
            } else {
                _uiState.update { it.copy(noApiKey = false) }
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val key = settings.apiKey.first()
            Log.d(TAG, "refresh: apiKey=${if (key.isNullOrBlank()) "NULL" else "OK"}")
            if (key.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        noApiKey = true,
                        isLoading = false,
                        error = "Configura tu API Key de REE en Ajustes",
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null, noApiKey = false) }

            try {
                getTodayPrices().collect { result ->
                    result
                        .onSuccess { day ->
                            Log.d(TAG, "todayPrices: OK, ${day.hours.size} horas")
                            _uiState.update { it.copy(today = day) }
                        }
                        .onFailure { e ->
                            Log.e(TAG, "todayPrices: FAIL", e)
                            _uiState.update { it.copy(error = mensajeError(e)) }
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "todayPrices: EXCEPTION", e)
                _uiState.update { it.copy(error = mensajeError(e)) }
            }

            try {
                getTomorrowPrices().collect { result ->
                    result
                        .onSuccess { day ->
                            Log.d(TAG, "tomorrowPrices: OK, ${day?.hours?.size ?: 0} horas")
                            _uiState.update { it.copy(
                                tomorrow = day,
                                tomorrowNotReady = day == null,
                                isLoading = false,
                            )}
                        }
                        .onFailure { e ->
                            Log.e(TAG, "tomorrowPrices: FAIL", e)
                            _uiState.update { it.copy(isLoading = false) }
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "tomorrowPrices: EXCEPTION", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            Log.d(TAG, "saveApiKey: length=${key.length}")
            settings.setApiKey(key)
            _uiState.update { it.copy(noApiKey = false, error = null) }
            refresh()
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            Log.d(TAG, "clearApiKey")
            settings.clearApiKey()
            _uiState.update {
                it.copy(
                    noApiKey = true,
                    today = null,
                    tomorrow = null,
                    isLoading = false,
                    error = "Configura tu API Key de REE en Ajustes",
                )
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
