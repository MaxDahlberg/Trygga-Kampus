package com.example.tryggakampus.presentation.settingsPage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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