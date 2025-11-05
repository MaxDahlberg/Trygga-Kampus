package com.example.tryggakampus.presentation.authentication.registerPage

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tryggakampus.LocalNavController
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun provideContent(viewModel: TestRegisterViewModel) {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavController provides navController) {
                RegisterPage(viewModel = viewModel)
            }
        }
    }

    @Test
    fun initialStateShowsFormWithDisabledButton() {
        val viewModel = TestRegisterViewModel()
        provideContent(viewModel)

        composeTestRule.onNodeWithContentDescription("Trygga Kampus Logo").assertExists()
        composeTestRule.onNodeWithTag("email_input").assertTextEquals("")
        composeTestRule.onNodeWithTag("password_input").assertTextEquals("")
        composeTestRule.onNodeWithTag("sign_up_button").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Already registered?").assertExists()
        composeTestRule.onNodeWithTag("sign_in_button").assertExists()
    }

    @Test
    fun validInputEnablesSignUpButton() {
        val viewModel = TestRegisterViewModel()
        provideContent(viewModel)

        composeTestRule.onNodeWithTag("email_input").performClick().performTextInput("test@example.com")
        viewModel.setEmailForTest("test@example.com")

        composeTestRule.onNodeWithTag("password_input").performClick().performTextInput("Password123!")
        viewModel.setPasswordForTest("Password123!")

        composeTestRule.onNodeWithTag("sign_up_button").assertIsEnabled()
    }

    @Test
    fun invalidInputKeepsButtonDisabled() {
        val viewModel = TestRegisterViewModel()
        provideContent(viewModel)

        composeTestRule.onNodeWithTag("email_input").performClick().performTextInput("invalid")
        viewModel.setEmailForTest("invalid")

        composeTestRule.onNodeWithTag("password_input").performClick().performTextInput("short")
        viewModel.setPasswordForTest("short")

        composeTestRule.onNodeWithTag("sign_up_button").assertIsNotEnabled()
    }

    @Test
    fun signUpClickShowsLoadingSpinner() {
        val viewModel = TestRegisterViewModel()
        provideContent(viewModel)

        composeTestRule.onNodeWithTag("email_input").performClick().performTextInput("test@example.com")
        viewModel.setEmailForTest("test@example.com")
        composeTestRule.onNodeWithTag("password_input").performClick().performTextInput("Password123!")
        viewModel.setPasswordForTest("Password123!")

        composeTestRule.onNodeWithTag("sign_up_button").performClick()
        composeTestRule.onNodeWithContentDescription("Loading").assertExists()
    }

    @Test
    fun errorStateShowsErrorBox() {
        val viewModel = TestRegisterViewModel().apply {
            setErrorForTest("Test error")
        }
        provideContent(viewModel)

        composeTestRule.onNodeWithText("Test error").assertExists()
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Test error").assertDoesNotExist()
    }

    @Test
    fun footerSignInButtonIsClickable() {
        val viewModel = TestRegisterViewModel()
        provideContent(viewModel)

        composeTestRule.onNodeWithTag("sign_in_button").performClick()
    }
}