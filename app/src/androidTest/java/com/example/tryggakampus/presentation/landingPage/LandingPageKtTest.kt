package com.example.tryggakampus.presentation.landingPage

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.R
import com.example.tryggakampus.ui.theme.TryggaKampusTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandingPageKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun resString(id: Int): String = ApplicationProvider.getApplicationContext<Application>().getString(id)

    @Test
    fun landingPageTitleRendering() {
        val title = "Test Title"
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            TryggaKampusTheme {
                CompositionLocalProvider(LocalNavController provides navController) {
                    LandingPage(title = title)
                }
            }
        }

        composeTestRule.onNodeWithText(title).assertExists()
    }

    @Test
    fun landingPageComponentComposition() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            TryggaKampusTheme {
                CompositionLocalProvider(LocalNavController provides navController) {
                    LandingPage(title = "Test")
                }
            }
        }

        composeTestRule.onNodeWithText(resString(R.string.title)).assertExists()
        composeTestRule.onNodeWithText(resString(R.string.about_us)).assertExists()
        composeTestRule.onNodeWithText(resString(R.string.button)).assertExists()
    }

    @Test
    fun landingPageWithEmptyTitle() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            TryggaKampusTheme {
                CompositionLocalProvider(LocalNavController provides navController) {
                    LandingPage(title = "")
                }
            }
        }

        composeTestRule.onNodeWithText(resString(R.string.about_us)).assertExists()
        composeTestRule.onNodeWithText("").assertExists()
    }

    @Test
    fun landingPageWithVeryLongTitle() {
        val longTitle = "This is a very long title that should wrap or truncate in the UI"
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            TryggaKampusTheme {
                CompositionLocalProvider(LocalNavController provides navController) {
                    LandingPage(title = longTitle)
                }
            }
        }

        composeTestRule.onNodeWithText(longTitle).assertExists()
    }

    @Test
    fun logoComponentVisibility() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            TryggaKampusTheme {
                CompositionLocalProvider(LocalNavController provides navController) {
                    LandingPage(title = "Test")
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Logo").assertExists()
        composeTestRule.onNodeWithText(resString(R.string.title)).assertExists()
    }

    @Test
    fun getHelpButtonClickNavigation() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        composeTestRule.setContent {
            TryggaKampusTheme {
                CompositionLocalProvider(LocalNavController provides navController) {
                    LandingPage(title = "Test")
                }
            }
        }

        // Click the button and assert navigation (adjust the expected route based on LandingPage implementation)
        composeTestRule.onNodeWithText("Get in touch!").performClick()
        // Example assertion: replace with actual route if known
        assert(navController.currentDestination?.route == "expected_route")
    }

    @Test
    fun getHelpButtonState() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            TryggaKampusTheme {
                CompositionLocalProvider(LocalNavController provides navController) {
                    LandingPage(title = "Test")
                }
            }
        }

        composeTestRule.onNodeWithText("Get in touch!").assertIsEnabled()
    }
}
