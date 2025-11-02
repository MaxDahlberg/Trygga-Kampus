package com.example.tryggakampus.presentation.authentication.loginPage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tryggakampus.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun initialStateShowsFormWithDisabledButton() {
        val viewModel = TestLoginViewModel()

        composeTestRule.setContent {
            LoginPage(viewModel = viewModel)
        }

        // Header logo
        composeTestRule.onNodeWithContentDescription("Trygga Kampus Logo").assertExists()

        // Inputs empty
        composeTestRule.onNodeWithTag("email_input").assertTextEquals("")
        composeTestRule.onNodeWithTag("password_input").assertTextEquals("")

        // Button disabled
        composeTestRule.onNodeWithTag("sign_in_button").assertIsNotEnabled()

        // Footer
        composeTestRule.onNodeWithText("Need an account?").assertExists()
        composeTestRule.onNodeWithTag("sign_up_button").assertExists()
    }

    @Test
    fun validInputEnablesSignInButton() {
        val viewModel = TestLoginViewModel()

        composeTestRule.setContent {
            LoginPage(viewModel = viewModel)
        }

        // Valid email
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        viewModel.onEmailChange("test@example.com")  // Sync

        // Valid password
        composeTestRule.onNodeWithTag("password_input").performTextInput("Password123!")
        viewModel.onPasswordChange("Password123!")

        // Button enabled
        composeTestRule.onNodeWithTag("sign_in_button").assertIsEnabled()
    }

    @Test
    fun invalidInputKeepsButtonDisabled() {
        val viewModel = TestLoginViewModel()

        composeTestRule.setContent {
            LoginPage(viewModel = viewModel)
        }

        // Invalid email
        composeTestRule.onNodeWithTag("email_input").performTextInput("invalid")
        viewModel.onEmailChange("invalid")

        // Invalid password
        composeTestRule.onNodeWithTag("password_input").performTextInput("short")
        viewModel.onPasswordChange("short")

        composeTestRule.onNodeWithTag("sign_in_button").assertIsNotEnabled()
    }

    @Test
    fun signInClickShowsLoadingSpinner() {
        val viewModel = TestLoginViewModel()

        composeTestRule.setContent {
            LoginPage(viewModel = viewModel)
        }

        // Valid input
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        viewModel.onEmailChange("test@example.com")
        composeTestRule.onNodeWithTag("password_input").performTextInput("Password123!")
        viewModel.onPasswordChange("Password123!")

        // Click sign in
        composeTestRule.onNodeWithTag("sign_in_button").performClick()

        // Spinner shows
        composeTestRule.onNodeWithContentDescription("Loading").assertExists()
    }

    @Test
    fun forgotPasswordShowsSuccessBox() {
        val viewModel = TestLoginViewModel()

        composeTestRule.setContent {
            LoginPage(viewModel = viewModel)
        }

        // Enter valid email
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        viewModel.onEmailChange("test@example.com")

        // Click forgot
        composeTestRule.onNodeWithTag("forgot_password_button").performClick()

        // Success box
        composeTestRule.onNodeWithText("A password reset link has been sent to your email.").assertExists()

        // Dismiss
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("A password reset link has been sent to your email.").assertDoesNotExist()
    }

    @Test
    fun errorStateShowsErrorBox() {
        val viewModel = TestLoginViewModel().apply {
            setErrorForTest("Test error")
        }

        composeTestRule.setContent {
            LoginPage(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Test error").assertExists()

        // Dismiss
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Test error").assertDoesNotExist()
    }

    @Test
    fun socialLoginClicksTriggerViewModelMethods() {
        val viewModel = TestLoginViewModel()

        composeTestRule.setContent {
            LoginPage(viewModel = viewModel)
        }

        // Google click
        composeTestRule.onNodeWithTag("google_icon").performClick()

        // Facebook click
        composeTestRule.onNodeWithTag("facebook_icon").performClick()
    }

    @Test
    fun footerSignUpButtonIsClickable() {
        val viewModel = TestLoginViewModel()

        composeTestRule.setContent {
            LoginPage(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag("sign_up_button").performClick()

        // Verify navigation (add NavController test if needed)
    }
}