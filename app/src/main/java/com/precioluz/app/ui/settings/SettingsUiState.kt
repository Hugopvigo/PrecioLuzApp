package com.precioluz.app.ui.settings

data class SettingsUiState(
    val notifyTomorrow: Boolean = true,
    val notifyDaily: Boolean = true,
    val notifyTomorrowHour: Int = 20,
    val notifyTomorrowMinute: Int = 15,
    val notifyDailyHour: Int = 8,
    val notifyDailyMinute: Int = 0,
    val darkTheme: DarkThemePreference = DarkThemePreference.SYSTEM
)

enum class DarkThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}
