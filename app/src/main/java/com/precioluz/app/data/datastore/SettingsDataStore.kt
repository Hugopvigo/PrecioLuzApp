package com.precioluz.app.data.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AppTheme { AUTO, LIGHT, DARK }

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    // ── Tema (DataStore normal) ──────────────────────────────────────
    private val THEME_KEY = stringPreferencesKey("app_theme")

    val theme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        AppTheme.entries.firstOrNull { it.name == prefs[THEME_KEY] } ?: AppTheme.AUTO
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[THEME_KEY] = theme.name }
    }

    // ── API Key (EncryptedSharedPreferences + StateFlow reactivo) ────
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "settings_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val _apiKey = MutableStateFlow(encryptedPrefs.getString("ree_api_key", null))
    val apiKey: Flow<String?> = _apiKey.asStateFlow()

    fun getApiKeySync(): String? = _apiKey.value

    suspend fun setApiKey(key: String) {
        encryptedPrefs.edit().putString("ree_api_key", key).apply()
        _apiKey.value = key
    }

    suspend fun clearApiKey() {
        encryptedPrefs.edit().remove("ree_api_key").apply()
        _apiKey.value = null
    }
}
