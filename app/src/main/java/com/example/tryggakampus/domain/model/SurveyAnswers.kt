package com.example.tryggakampus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SurveyAnswer(
    val question: String,
    val answer: String
)

@Serializable
data class SurveySubmission(
    val userId: String,
    val answers: List<SurveyAnswer>,
    val timestamp: Long = System.currentTimeMillis()
)