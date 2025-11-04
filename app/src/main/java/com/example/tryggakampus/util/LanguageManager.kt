package com.example.tryggakampus.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.util.*
import androidx.core.content.edit

object LanguageManager {

    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "app_language"

    fun setAppLanguage(context: Context, languageCode: String) {
        saveLanguagePreference(context, languageCode)
        applyLanguage(context, languageCode)
        restartActivity(context)
    }

    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit { putString(KEY_LANGUAGE, languageCode) }
    }

    fun getSavedLanguage(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_LANGUAGE, Locale.getDefault().language) ?: "en"
    }

    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    private fun restartActivity(context: Context) {
        if (context is Activity) {
            val intent = Intent(context, context::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
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