package com.example.tryggakampus.util


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.util.*

object LanguageManager {

    fun setAppLanguage(context: Context, languageCode: String) {
        // Save preference to DataStore
        saveLanguagePreference(context, languageCode)

        // Apply language
        applyLanguage(context, languageCode)

        // Restart activity
        restartActivity(context)
    }

    private fun saveLanguagePreference(context: Context, languageCode: String) {

        val sharedPref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sharedPref.edit().putString("app_language", languageCode).apply()
    }

    fun getSavedLanguage(context: Context): String {
        val sharedPref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return sharedPref.getString("app_language", Locale.getDefault().language) ?: "en"
    }

    private fun applyLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        context.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun restartActivity(context: Context) {
        if (context is Activity) {
            val intent = Intent(context, context::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            context.finish()
        }
    }

    @Composable
    fun InitializeLanguage() {
        val context = LocalContext.current
        val savedLanguage = getSavedLanguage(context)

        LaunchedEffect(savedLanguage) {
            if (savedLanguage != Locale.getDefault().language) {
                applyLanguage(context, savedLanguage)
            }
        }
    }
}