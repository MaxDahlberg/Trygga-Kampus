package com.example.tryggakampus.util

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Theme options
enum class AppThemeOption {
    LIGHT, DARK, SYSTEM
}

// DataStore setup
private val Context.dataStore by preferencesDataStore("settings")
private val THEME_KEY = intPreferencesKey("app_theme")

object ThemePreferences {

    // Save theme
    suspend fun saveTheme(context: Context, theme: AppThemeOption) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme.ordinal
        }
    }

    // Read theme
    fun getTheme(context: Context): Flow<AppThemeOption> =
        context.dataStore.data.map { prefs ->
            val ordinal = prefs[THEME_KEY] ?: AppThemeOption.SYSTEM.ordinal
            AppThemeOption.entries[ordinal]
        }
}

object ThemeManager {

    private val _currentTheme = MutableStateFlow(AppThemeOption.SYSTEM)
    val currentTheme: StateFlow<AppThemeOption> = _currentTheme.asStateFlow()

     // Set the current theme immediately and persist it
    fun setTheme(theme: AppThemeOption, context: Context? = null) {
        _currentTheme.value = theme
        context?.let { ctx ->
            CoroutineScope(Dispatchers.IO).launch {
                ThemePreferences.saveTheme(ctx, theme)
            }
        }
    }
}
