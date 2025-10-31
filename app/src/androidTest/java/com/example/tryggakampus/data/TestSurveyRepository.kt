package com.example.tryggakampus.data

import com.example.tryggakampus.domain.model.SurveyAnswer

class TestSurveyRepository(
    private val shouldSucceed: Boolean = true,
    private val errorMessage: String = "Test error"
) : SurveyRepository() {
    override suspend fun submitSurveyAnswers(answers: List<SurveyAnswer>) {
        if (!shouldSucceed) throw Exception(errorMessage)
        // succeed (no-op)
    }
}
