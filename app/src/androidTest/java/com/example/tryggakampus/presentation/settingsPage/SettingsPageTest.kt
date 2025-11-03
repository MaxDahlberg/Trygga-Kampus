package com.example.tryggakampus.presentation.settingsPage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tryggakampus.util.AppThemeOption
import com.example.tryggakampus.util.LanguageManager
import com.example.tryggakampus.util.ThemeManager
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        mockkStatic(LanguageManager::class)
        mockkStatic(ThemeManager::class)
        every { ThemeManager.currentTheme } returns MutableStateFlow(AppThemeOption.LIGHT)
    }

    @Test
    fun initialStateShowsLanguageAndThemeSections() {
        composeTestRule.setContent {
            SettingsPage(title = "Settings")
        }

        composeTestRule.onNodeWithText("App Language").assertExists()
        composeTestRule.onNodeWithText("App Theme").assertExists()
    }

    @Test
    fun languageButtonClickCallsOnLanguageSelected() {
        composeTestRule.setContent {
            SettingsPage(title = "Settings")
        }

        composeTestRule.onNodeWithText("FR").performClick()

    }

    @Test
    fun themeButtonClickCallsOnThemeSelected() {
        composeTestRule.setContent {
            SettingsPage(title = "Settings")
        }

        composeTestRule.onNodeWithText("Dark").performClick()

    }
}