@file:Suppress("unused")

package com.example.tryggakampus.data

fun makeTestSurveyRepository(
    shouldSucceed: Boolean = true,
    errorMessage: String = "Test error"
): SurveyRepository {
    // Provide a simple fake implementation for tests
    return object : SurveyRepository() {
        override suspend fun submitSurveyAnswers(answers: List<com.example.tryggakampus.domain.model.SurveyAnswer>) {
            if (!shouldSucceed) throw Exception(errorMessage)
            // else no-op to simulate success
        }
    }
}
