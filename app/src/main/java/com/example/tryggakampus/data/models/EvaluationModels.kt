package com.example.tryggakampus.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Evaluation(
    val id: String = "",
    val userId: String = "",
    val type: EvaluationType = EvaluationType.MORNING_CHECK_IN,
    @ServerTimestamp val completedAt: Timestamp? = null,
    val answers: Map<String, Any> = emptyMap(),
    val selfEsteemScore: Int? = null
)

enum class EvaluationType {
    MORNING_CHECK_IN,
    EVENING_REFLECTION,
    WEEKLY_REVIEW,
    MONTHLY_EVALUATION
}

data class Question(
    val id: String,
    val text: String,
    val type: QuestionType,
    val options: List<String> = emptyList()
)

enum class QuestionType {
    SLIDER_1_5,
    SLIDER_1_10,
    TEXT_INPUT,
    MULTIPLE_CHOICE
}
