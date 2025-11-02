package com.example.tryggakampus.presentation.authentication.registerPage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun initialStateShowsFormWithDisabledButton() {
        val viewModel = TestRegisterViewModel()

        composeTestRule.setContent {
            RegisterPage(viewModel = viewModel)
        }

        // Header logo
        composeTestRule.onNodeWithContentDescription("Trygga Kampus Logo").assertExists()

        // Inputs empty
        composeTestRule.onNodeWithTag("email_input").assertTextEquals("")
        composeTestRule.onNodeWithTag("password_input").assertTextEquals("")

        // Button disabled
        composeTestRule.onNodeWithTag("sign_up_button").assertIsNotEnabled()

        // Footer
        composeTestRule.onNodeWithText("Already registered?").assertExists()
        composeTestRule.onNodeWithTag("sign_in_button").assertExists()
    }

    @Test
    fun validInputEnablesSignUpButton() {
        val viewModel = TestRegisterViewModel()

        composeTestRule.setContent {
            RegisterPage(viewModel = viewModel)
        }

        // Valid email
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        viewModel.setEmailForTest("test@example.com")

        // Valid password
        composeTestRule.onNodeWithTag("password_input").performTextInput("Password123!")
        viewModel.setPasswordForTest("Password123!")

        // Button enabled
        composeTestRule.onNodeWithTag("sign_up_button").assertIsEnabled()
    }

    @Test
    fun invalidInputKeepsButtonDisabled() {
        val viewModel = TestRegisterViewModel()

        composeTestRule.setContent {
            RegisterPage(viewModel = viewModel)
        }

        // Invalid email
        composeTestRule.onNodeWithTag("email_input").performTextInput("invalid")
        viewModel.setEmailForTest("invalid")

        // Invalid password
        composeTestRule.onNodeWithTag("password_input").performTextInput("short")
        viewModel.setPasswordForTest("short")

        composeTestRule.onNodeWithTag("sign_up_button").assertIsNotEnabled()
    }

    @Test
    fun signUpClickShowsLoadingSpinner() {
        val viewModel = TestRegisterViewModel()

        composeTestRule.setContent {
            RegisterPage(viewModel = viewModel)
        }

        // Valid input
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        viewModel.setEmailForTest("test@example.com")
        composeTestRule.onNodeWithTag("password_input").performTextInput("Password123!")
        viewModel.setPasswordForTest("Password123!")

        // Click sign up
        composeTestRule.onNodeWithTag("sign_up_button").performClick()

        // Spinner shows
        composeTestRule.onNodeWithContentDescription("Loading").assertExists()
    }

    @Test
    fun errorStateShowsErrorBox() {
        val viewModel = TestRegisterViewModel().apply {
            setErrorForTest("Test error")
        }

        composeTestRule.setContent {
            RegisterPage(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Test error").assertExists()

        // Dismiss
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Test error").assertDoesNotExist()
    }

    @Test
    fun footerSignInButtonIsClickable() {
        val viewModel = TestRegisterViewModel()

        composeTestRule.setContent {
            RegisterPage(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag("sign_in_button").performClick()

        // Verify navigation (add NavController test if needed)
    }
}