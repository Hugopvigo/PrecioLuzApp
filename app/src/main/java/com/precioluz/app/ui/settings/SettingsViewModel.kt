package com.precioluz.app.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setNotifyTomorrow(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notifyTomorrow = enabled)
    }

    fun setNotifyDaily(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notifyDaily = enabled)
    }

    fun setDarkTheme(preference: DarkThemePreference) {
        _uiState.value = _uiState.value.copy(darkTheme = preference)
    }
}
