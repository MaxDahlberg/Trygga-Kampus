package com.example.tryggakampus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SurveyAnswer(
    val question: String,
    val answer: String
)