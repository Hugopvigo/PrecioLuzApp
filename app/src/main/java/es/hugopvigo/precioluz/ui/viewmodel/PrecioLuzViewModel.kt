package es.hugopvigo.precioluz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.hugopvigo.precioluz.data.datastore.AppTheme
import es.hugopvigo.precioluz.data.datastore.SettingsDataStore
import es.hugopvigo.precioluz.domain.model.DayPrices
import es.hugopvigo.precioluz.domain.usecase.GetTodayPricesUseCase
import es.hugopvigo.precioluz.domain.usecase.GetTomorrowPricesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrecioLuzUiState(
    val today: DayPrices?    = null,
    val tomorrow: DayPrices? = null,
    val isLoading: Boolean   = true,
    val error: String?       = null,
    val tomorrowNotReady: Boolean = false,  // precios no publicados aún
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

            getTodayPrices().collect { result ->
                result
                    .onSuccess { day -> _uiState.update { it.copy(today = day) } }
                    .onFailure { e  -> _uiState.update { it.copy(error = e.message) } }
            }

            getTomorrowPrices().collect { result ->
                result
                    .onSuccess { day ->
                        _uiState.update { it.copy(
                            tomorrow = day,
                            tomorrowNotReady = day == null,
                            isLoading = false,
                        )}
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                    }
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
