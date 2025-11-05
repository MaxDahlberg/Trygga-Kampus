package com.example.tryggakampus.presentation.profilePage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tryggakampus.ui.theme.TryggaKampusTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilePageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun initialStateShowsAccountInfoAndDisabledSections() {
        val viewModel = TestProfileViewModel()

        composeTestRule.setContent {
            TryggaKampusTheme {
                ProfilePage(viewModel = viewModel)
            }
        }

        // Header
        composeTestRule.onNodeWithTag("profile_header").assertExists()

        // Account info
        composeTestRule.onNodeWithTag("account_info_section").assertExists()
        composeTestRule.onNodeWithTag("username_text").assertExists()
        composeTestRule.onNodeWithTag("email_text").assertExists()

        // Hobbies section
        composeTestRule.onNodeWithTag("hobbies_section").assertExists()
        composeTestRule.onNodeWithTag("save_hobbies_button").assertExists()

        // Username change section (disabled if invalid)
        composeTestRule.onNodeWithTag("username_change_section").assertExists()
        composeTestRule.onNodeWithTag("update_username_button").assertIsNotEnabled()

        // Password change section
        composeTestRule.onNodeWithTag("password_change_section").assertExists()
        composeTestRule.onNodeWithTag("update_password_button").assertIsNotEnabled()

        // Account data section
        composeTestRule.onNodeWithTag("account_data_section").assertExists()
        composeTestRule.onNodeWithTag("request_data_button").assertExists()
        composeTestRule.onNodeWithTag("delete_account_button").assertExists()
    }

    @Test
    fun hobbiesToggleAndSaveWorks() {
        val viewModel = TestProfileViewModel().apply {
            setHobbiesForTest(emptySet())
        }

        composeTestRule.setContent {
            TryggaKampusTheme {
                ProfilePage(viewModel = viewModel)
            }
        }

        // Toggle hobby
        composeTestRule.onNodeWithTag("hobby_checkbox_hobby1").performClick()
        assertTrue(viewModel.hobbies.contains("hobby1"))

        // Save button click
        composeTestRule.onNodeWithTag("save_hobbies_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 1000) {
            composeTestRule.onAllNodesWithTag("hobbies_error_box").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun usernameChangeInputEnablesButton() {
        val viewModel = TestProfileViewModel()

        composeTestRule.setContent {
            TryggaKampusTheme {
                ProfilePage(viewModel = viewModel)
            }
        }

        // Valid password
        composeTestRule.onNodeWithTag("username_change_password_input").performTextInput("Password123!")
        viewModel.onUsernameChangePasswordChange("Password123!")

        // Valid new username
        composeTestRule.onNodeWithTag("new_username_input").performTextInput("newuser")
        viewModel.newUsername = "newuser"

        // Button enabled
        composeTestRule.onNodeWithTag("update_username_button").assertIsEnabled()
    }

    @Test
    fun passwordChangeInputEnablesButton() {
        val viewModel = TestProfileViewModel()

        composeTestRule.setContent {
            TryggaKampusTheme {
                ProfilePage(viewModel = viewModel)
            }
        }

        // Valid current password
        composeTestRule.onNodeWithTag("current_password_input").performTextInput("Current123!")
        viewModel.onCurrentPasswordChange("Current123!")

        // Valid new password
        composeTestRule.onNodeWithTag("new_password_input").performTextInput("NewPassword123!")
        viewModel.onNewPasswordChange("NewPassword123!")

        // Repeat new password
        composeTestRule.onNodeWithTag("repeat_password_input").performTextInput("NewPassword123!")
        viewModel.repeatNewPassword = "NewPassword123!"

        // Button enabled
        composeTestRule.onNodeWithTag("update_password_button").assertIsEnabled()
    }

    @Test
    fun requestDataButtonOpensDialog() {
        val viewModel = TestProfileViewModel()

        composeTestRule.setContent {
            TryggaKampusTheme {
                ProfilePage(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("request_data_button").performClick()

        // Dialog visible
        composeTestRule.onNodeWithTag("request_data_dialog").assertExists()

        // Click request
        composeTestRule.onNodeWithTag("request_data_button_dialog").performClick()

        // Dialog still open, now with download
        composeTestRule.waitForIdle()

        // Click download
        composeTestRule.onNodeWithTag("request_data_button_dialog").performClick()

        // Dialog closes
        composeTestRule.onNodeWithTag("request_data_dialog").assertDoesNotExist()
    }

    @Test
    fun deleteAccountButtonOpensConfirmDialog() {
        val viewModel = TestProfileViewModel()

        composeTestRule.setContent {
            TryggaKampusTheme {
                ProfilePage(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("delete_account_button").performClick()

        // Dialog visible
        composeTestRule.onNodeWithTag("delete_account_dialog").assertExists()

        // Enter password
        composeTestRule.onNodeWithTag("delete_password_input").performTextInput("Password123!")

        // Confirm
        composeTestRule.onNodeWithTag("confirm_delete_button").performClick()

        // Dialog closes, no error
        composeTestRule.onNodeWithTag("delete_account_dialog").assertDoesNotExist()
        composeTestRule.onNodeWithTag("delete_account_error_box").assertDoesNotExist()
    }

    @Test
    fun errorStateShowsErrorBox() {
        val viewModel = TestProfileViewModel().apply {
            setErrorForTest("Test error")
        }

        composeTestRule.setContent {
            TryggaKampusTheme {
                ProfilePage(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Test error").assertExists()

        // Dismiss
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Test error").assertDoesNotExist()
    }
}