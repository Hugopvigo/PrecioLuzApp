package com.precioluz.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.precioluz.app.data.datastore.SettingsDataStore
import com.precioluz.app.data.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsDataStore,
    private val scheduler: NotificationScheduler,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(settings.notifyTomorrow, settings.notifyDaily) { tomorrow, daily ->
            SettingsUiState(tomorrow, daily)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun setNotifyTomorrow(enabled: Boolean) {
        viewModelScope.launch {
            settings.setNotifyTomorrow(enabled)
            scheduler.sync()
        }
    }

    fun setNotifyDaily(enabled: Boolean) {
        viewModelScope.launch {
            settings.setNotifyDaily(enabled)
            scheduler.sync()
        }
    }

    fun sync() {
        viewModelScope.launch { scheduler.sync() }
    }
}
