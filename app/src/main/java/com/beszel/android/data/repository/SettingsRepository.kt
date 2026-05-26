package com.beszel.android.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore("beszel_settings")

data class AppSettings(
    val darkMode: Boolean = false,
    val accentColor: String = "teal",
    val density: String = "comfortable",
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val DARK_MODE    = booleanPreferencesKey("dark_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val DENSITY      = stringPreferencesKey("density")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            darkMode    = prefs[Keys.DARK_MODE] ?: false,
            accentColor = prefs[Keys.ACCENT_COLOR] ?: "teal",
            density     = prefs[Keys.DENSITY] ?: "comfortable",
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setAccentColor(color: String) {
        context.settingsDataStore.edit { it[Keys.ACCENT_COLOR] = color }
    }

    suspend fun setDensity(density: String) {
        context.settingsDataStore.edit { it[Keys.DENSITY] = density }
    }
}
