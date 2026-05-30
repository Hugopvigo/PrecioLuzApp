package es.hugopvigo.precioluz.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AppTheme { AUTO, LIGHT, DARK }

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val THEME_KEY = stringPreferencesKey("app_theme")

    val theme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        AppTheme.entries.firstOrNull { it.name == prefs[THEME_KEY] } ?: AppTheme.AUTO
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[THEME_KEY] = theme.name }
    }
}
