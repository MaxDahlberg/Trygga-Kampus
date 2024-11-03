package com.example.tryggakampus.presentation.settingsPage

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel

class SettingsPageViewModel: ViewModel() {
    var someTabIndex by mutableStateOf<ArticleTabs>(ArticleTabs.TAB_ONE)
        private set

    fun setTabIndex(t: ArticleTabs) {
        someTabIndex = t
    }
}

enum class ArticleTabs {
    TAB_ONE,
    TAB_TWO,
    TAB_THREE
}