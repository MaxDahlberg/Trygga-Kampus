package com.example.tryggakampus

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.tryggakampus.presentation.MainScreen
import com.example.tryggakampus.ui.theme.TryggaKampusTheme
import com.example.tryggakampus.util.LanguageManager
import com.example.tryggakampus.util.ThemeManager
import com.example.tryggakampus.util.ThemePreferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved language BEFORE super.onCreate
        applySavedLanguage()

        // Load saved theme synchronously before Compose starts
        runBlocking {
            val savedTheme = ThemePreferences.getTheme(this@MainActivity).first()
            ThemeManager.setTheme(savedTheme)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeState by ThemeManager.currentTheme.collectAsState()
            val isDark = when (themeState) {
                com.example.tryggakampus.util.AppThemeOption.LIGHT -> false
                com.example.tryggakampus.util.AppThemeOption.DARK -> true
                com.example.tryggakampus.util.AppThemeOption.SYSTEM ->
                    androidx.compose.foundation.isSystemInDarkTheme()
            }

            TryggaKampusTheme(darkTheme = isDark) {
                LanguageManager.InitializeLanguage()
                Navigation { navController, pageToView -> MainScreen(navController, pageToView) }
            }
        }
    }

    private fun applySavedLanguage() {
        val savedLanguage = LanguageManager.getSavedLanguage(this)
        val currentLanguage = Locale.getDefault().language

        if (savedLanguage != currentLanguage) {
            val locale = Locale(savedLanguage)
            Locale.setDefault(locale)
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            createConfigurationContext(configuration)
        }
    }
}