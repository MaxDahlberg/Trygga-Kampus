package com.example.tryggakampus.data

import io.mockk.coEvery
import io.mockk.mockk

fun makeTestSurveyRepository(
    shouldSucceed: Boolean = true,
    errorMessage: String = "Test error"
): SurveyRepository {
    val repo = mockk<SurveyRepository>(relaxed = true)
    coEvery { repo.submitSurveyAnswers(any()) } coAnswers {
        if (!shouldSucceed) throw Exception(errorMessage)
        else Unit
    }
    return repo
}
