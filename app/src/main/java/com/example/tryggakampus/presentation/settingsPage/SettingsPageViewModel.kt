// app/src/main/java/com/example/tryggakampus/presentation/settingsPage/SettingsPageViewModel.kt
package com.example.tryggakampus.presentation.settingsPage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tryggakampus.util.LanguageManager
import java.util.*

class SettingsPageViewModel : ViewModel() {
    var someTabIndex by mutableStateOf<ArticleTabs>(ArticleTabs.TAB_ONE)
        private set

    // Initialize with the current system language
    var currentLanguage: String = Locale.getDefault().language
        private set

    fun setTabIndex(t: ArticleTabs) {
        someTabIndex = t
    }

    fun setLanguage(languageCode: String) {
        currentLanguage = languageCode
    }
}

enum class ArticleTabs {
    TAB_ONE,
    TAB_TWO,
    TAB_THREE
}