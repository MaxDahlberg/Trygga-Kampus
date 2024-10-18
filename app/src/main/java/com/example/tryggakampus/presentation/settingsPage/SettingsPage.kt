package com.example.tryggakampus.presentation.settingsPage

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.presentation.component.PageContainer

@Composable
fun SettingsPage(modelView: SettingsPageViewModel = viewModel<SettingsPageViewModel>(), title: String) {
    PageContainer {
        Text(title)

        when (modelView.someTabIndex) {
            ArticleTabs.TAB_ONE -> Text("Viewing tab one")
            ArticleTabs.TAB_TWO -> Text("Viewing tab two")
            ArticleTabs.TAB_THREE -> Text("Viewing tab three")
        }
    }
}
