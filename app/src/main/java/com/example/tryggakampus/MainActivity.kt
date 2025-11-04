package com.example.tryggakampus

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.tryggakampus.presentation.MainScreen
import com.example.tryggakampus.ui.theme.TryggaKampusTheme
import com.example.tryggakampus.util.LanguageManager
import com.example.tryggakampus.util.ThemeManager
import com.example.tryggakampus.util.ThemePreferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Get saved language and wrap context with it
        val langCode = LanguageManager.getSavedLanguage(newBase)
        val localizedContext = LanguageManager.applyLanguage(newBase, langCode)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme synchronously before UI loads
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
                com.example.tryggakampus.util.AppThemeOption.SYSTEM -> isSystemInDarkTheme()
            }

            TryggaKampusTheme(darkTheme = isDark) {
                LanguageManager.InitializeLanguage()
                Navigation { navController, pageToView ->
                    MainScreen(navController, pageToView)
                }
            }
        }
    }
}