package com.example.tryggakampus.data.repository

import com.example.tryggakampus.domain.model.SurveyAnswer
import com.example.tryggakampus.domain.model.SurveySubmission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SurveyRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("Student-Survey-Submissions")

    suspend fun submitSurveyAnswers(answers: List<SurveyAnswer>) {
        val currentUser = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("User not signed in")

        val submission = SurveySubmission(
            userId = currentUser.uid,
            answers = answers
        )

        // Using UID as document ID to enforce one submission per user
        collection.document(currentUser.uid)
            .set(submission)
            .await()
    }
}