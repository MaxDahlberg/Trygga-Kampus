package com.example.tryggakampus.presentation.surveyPage

import com.example.tryggakampus.data.makeTestSurveyRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SurveyViewModelTest {

    private val testRepo = makeTestSurveyRepository(shouldSucceed = true)
    private val viewModel = SurveyViewModel(testRepo)

    @Test
    fun `submitSurvey maps and calls repo without crash`() = runTest {
        val questions = listOf("Q1?", "Q2?")
        val answers = listOf("A1", "A2")

        viewModel.submitSurvey(questions, answers)

    }

    @Test(expected = Exception::class)
    fun `submitSurvey throws on repo failure`() = runTest {
        val failingRepo = makeTestSurveyRepository(shouldSucceed = false)
        val failingViewModel = SurveyViewModel(failingRepo)

        failingViewModel.submitSurvey(listOf("Q?"), listOf("A"))
    }
}