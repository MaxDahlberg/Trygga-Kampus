package com.example.tryggakampus

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.tryggakampus.presentation.MainScreen
import com.example.tryggakampus.ui.theme.TryggaKampusTheme
import com.example.tryggakampus.util.LanguageManager
import java.util.*
import android.content.res.Configuration
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved language BEFORE super.onCreate
        applySavedLanguage()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TryggaKampusTheme {
                // Initialize and watch for language changes
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