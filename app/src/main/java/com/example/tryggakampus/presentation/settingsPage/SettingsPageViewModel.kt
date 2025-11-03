package com.example.tryggakampus.presentation.settingsPage

import androidx.lifecycle.ViewModel
import java.util.*

class SettingsPageViewModel : ViewModel() {

    // Initialize with the current system language
    var currentLanguage: String = Locale.getDefault().language
        private set

    fun setLanguage(languageCode: String) {
        currentLanguage = languageCode
    }
}
