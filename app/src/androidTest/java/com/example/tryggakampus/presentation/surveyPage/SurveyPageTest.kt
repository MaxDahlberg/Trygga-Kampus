package com.example.tryggakampus.presentation.surveyPage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tryggakampus.data.TestSurveyRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SurveyPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun submitting_valid_form_shows_success_snackbar_and_clears_fields() {
        val testRepo = TestSurveyRepository(shouldSucceed = true)
        val testViewModel = SurveyViewModel(testRepo)

        composeTestRule.setContent {
            SurveyPage(title = "Test Survey", viewModel = testViewModel)
        }

        composeTestRule.onNodeWithTag("answer_0").performTextInput("Answer 1")
        composeTestRule.onNodeWithTag("answer_1").performTextInput("Answer 2")

        composeTestRule.onNodeWithText("Submit Answers").performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodes(hasText("Survey submitted successfully!"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Survey submitted successfully!", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("answer_0").assertTextEquals("")
        composeTestRule.onNodeWithTag("answer_1").assertTextEquals("")
        composeTestRule.onNodeWithText("Submit Answers").assertIsNotEnabled()
    }

    @Test
    fun submitting_fails_shows_error_snackbar() {
        val testRepo = TestSurveyRepository(shouldSucceed = false, errorMessage = "Submission failed")
        val testViewModel = SurveyViewModel(testRepo)

        composeTestRule.setContent {
            SurveyPage(title = "Test Survey", viewModel = testViewModel)
        }

        composeTestRule.onNodeWithTag("answer_0").performTextInput("Answer 1")
        composeTestRule.onNodeWithTag("answer_1").performTextInput("Answer 2")

        composeTestRule.onNodeWithText("Submit Answers").performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodes(hasText("Failed to submit: Submission failed"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Failed to submit: Submission failed", useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
