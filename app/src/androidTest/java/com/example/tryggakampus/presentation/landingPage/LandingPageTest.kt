package com.example.tryggakampus.presentation.landingPage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tryggakampus.R
import com.example.tryggakampus.Routes
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandingPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun initialStateShowsLogoAboutUsAndGetHelpSections() {
        composeTestRule.setContent {
            LandingPage(title = "Landing")
        }

        // Logo section
        composeTestRule.onNodeWithTag("logo_section").assertExists()
        composeTestRule.onNodeWithTag("logo_image").assertExists()
        composeTestRule.onNodeWithTag("title_text").assertExists()

        // About Us section
        composeTestRule.onNodeWithTag("about_us_section").assertExists()
        composeTestRule.onNodeWithTag("about_us_title_text").assertExists()
        composeTestRule.onNodeWithTag("about_us_content_text").assertExists()

        // Get Help section
        composeTestRule.onNodeWithTag("get_help_section").assertExists()
        composeTestRule.onNodeWithTag("get_help_title_text").assertExists()
        composeTestRule.onNodeWithTag("get_help_content_text").assertExists()
        composeTestRule.onNodeWithTag("get_help_button").assertExists()
    }

    @Test
    fun getHelpButtonNavigatesToFormPage() {
        val mockNavController = mockk<NavController>(relaxed = true)

        composeTestRule.setContent {
            LandingPage(title = "Landing")
        }

        // Click get help button
        composeTestRule.onNodeWithTag("get_help_button").performClick()

        // Verify navigation
        verify { mockNavController.navigate(Routes.FormPage) }
    }

    @Test
    fun layoutRendersCorrectlyWithoutErrors() {
        composeTestRule.setContent {
            LandingPage(title = "Landing")
        }

        // Use the activity context to get string resources in tests
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.about_us)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.button)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Get in touch!").assertIsDisplayed()
    }
}
