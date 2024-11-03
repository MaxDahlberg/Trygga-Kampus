package com.example.tryggakampus.data

import com.example.tryggakampus.domain.model.SurveyAnswer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SurveyRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun submitSurveyAnswers(surveys: List<SurveyAnswer>) {
        try {
            val surveyCollection = db.collection("Student-Survey-Answers")
            surveys.forEach { survey ->
                surveyCollection.add(survey).await()
            }
        } catch (e: Exception) {
            throw e
        }
    }
}