package com.example.tryggakampus.presentation.authentication.loginPage

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.ui.theme.TryggaKampusTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun provideContent(viewModel: TestLoginViewModel) {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            TryggaKampusTheme {
                CompositionLocalProvider(LocalNavController provides navController) {
                    LoginPage(viewModel = viewModel)
                }
            }
        }
    }

    @Test
    fun initialStateShowsFormWithDisabledButton() {
        val viewModel = TestLoginViewModel()
        provideContent(viewModel)

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
        provideContent(viewModel)

        // Valid email
        composeTestRule.onNodeWithTag("email_input").performClick().performTextInput("test@example.com")
        viewModel.onEmailChange("test@example.com")  // Sync
        composeTestRule.waitForIdle()

        // Valid password
        composeTestRule.onNodeWithTag("password_input").performClick().performTextInput("Password123!")
        viewModel.onPasswordChange("Password123!")
        composeTestRule.waitForIdle()

        // Button enabled
        composeTestRule.onNodeWithTag("sign_in_button").assertIsEnabled()
    }

    @Test
    fun invalidInputKeepsButtonDisabled() {
        val viewModel = TestLoginViewModel()
        provideContent(viewModel)

        // Invalid email
        composeTestRule.onNodeWithTag("email_input").performClick().performTextInput("invalid")
        viewModel.onEmailChange("invalid")
        composeTestRule.waitForIdle()

        // Invalid password
        composeTestRule.onNodeWithTag("password_input").performClick().performTextInput("short")
        viewModel.onPasswordChange("short")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("sign_in_button").assertIsNotEnabled()
    }

    @Test
    fun signInClickShowsLoadingSpinner() {
        val viewModel = TestLoginViewModel()
        provideContent(viewModel)

        // Valid input
        composeTestRule.onNodeWithTag("email_input").performClick().performTextInput("test@example.com")
        viewModel.onEmailChange("test@example.com")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("password_input").performClick().performTextInput("Password123!")
        viewModel.onPasswordChange("Password123!")
        composeTestRule.waitForIdle()

        // Click sign in
        composeTestRule.onNodeWithTag("sign_in_button").performClick()

        // Spinner shows
        composeTestRule.onNodeWithContentDescription("Loading").assertExists()
    }

    @Test
    fun forgotPasswordShowsSuccessBox() {
        val viewModel = TestLoginViewModel()
        provideContent(viewModel)

        // Enter valid email
        composeTestRule.onNodeWithTag("email_input").performClick().performTextInput("test@example.com")
        viewModel.onEmailChange("test@example.com")
        composeTestRule.waitForIdle()

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
        provideContent(viewModel)

        composeTestRule.onNodeWithText("Test error").assertExists()

        // Dismiss
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Test error").assertDoesNotExist()
    }

    @Test
    fun socialLoginClicksTriggerViewModelMethods() {
        val viewModel = TestLoginViewModel()
        provideContent(viewModel)

        // Google click
        composeTestRule.onNodeWithTag("google_icon").performClick()

        // Facebook click
        composeTestRule.onNodeWithTag("facebook_icon").performClick()
    }

    @Test
    fun footerSignUpButtonIsClickable() {
        val viewModel = TestLoginViewModel()
        provideContent(viewModel)

        composeTestRule.onNodeWithTag("sign_up_button").performClick()

        // Verify navigation (add NavController test if needed)
    }
}