package com.example.tryggakampus.util

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

class GdprUserDataHelper {

    private val db = Firebase.firestore
    private val rtdb = FirebaseDatabase.getInstance().reference

    suspend fun fetchUserData(userId: String): String {
        // Firestore: User information
        val userInfoDoc = db.collection("user-information")
            .document(userId)
            .get()
            .await()
        val userInfo = userInfoDoc.data

        // Firestore: Survey submissions
        val surveyDocs = db.collection("Student-Survey-Submissions")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        val surveys = surveyDocs.documents.map { it.data }

        // Firestore: Stories
        val storyDocs = db.collection("student-stories")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        val stories = storyDocs.documents.map { it.data }

        // Firestore: Comments
        val commentDocs = db.collection("story-comments")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        val comments = commentDocs.documents.map { it.data }

        // Firestore: Self-assessments
        val selfAssessmentsDocs = db.collection("users")
            .document(userId)
            .collection("selfAssessments")
            .get()
            .await()
        val selfAssessments = selfAssessmentsDocs.documents.map { it.data }

        // Firestore: Video feedback
        val videoFeedbackDocs = db.collection("users")
            .document(userId)
            .collection("videoFeedback")
            .get()
            .await()
        val videoFeedback = videoFeedbackDocs.documents.map { it.data }

        // Realtime Database: Voice notes
        val voiceNotesSnapshot = rtdb.child("voice_notes").child(userId).get().await()
        val typeIndicator = object : GenericTypeIndicator<Map<String, Map<String, Any>>>() {}
        val voiceNotesMap: Map<String, Map<String, Any>>? = voiceNotesSnapshot.getValue(typeIndicator)
        val voiceNotesList: List<Map<String, Any>> = voiceNotesMap?.values?.toList() ?: emptyList()

        // Combine all data into a single JSON
        val fullData = mapOf(
            "userInformation" to userInfo,
            "surveySubmissions" to surveys,
            "stories" to stories,
            "comments" to comments,
            "selfAssessments" to selfAssessments,
            "videoFeedback" to videoFeedback,
            "voiceNotes" to voiceNotesList
        )

        return Gson().toJson(fullData)
    }

    suspend fun deleteUserData(userId: String) {
        // Delete stories and their comments
        val storyDocs = db.collection("student-stories")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        for (story in storyDocs.documents) {
            val storyId = story.id

            // Delete all comments for this story
            val storyComments = db.collection("story-comments")
                .whereEqualTo("storyId", storyId)
                .get()
                .await()
            storyComments.documents.forEach { it.reference.delete().await() }

            // Delete the story itself
            story.reference.delete().await()
        }

        // Delete comments authored by the user that aren't tied to their stories
        val userComments = db.collection("story-comments")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        userComments.documents.forEach { it.reference.delete().await() }

        // Delete survey submissions
        val surveyDocs = db.collection("Student-Survey-Submissions")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        surveyDocs.documents.forEach { it.reference.delete().await() }

        // Delete user information
        db.collection("user-information")
            .document(userId)
            .delete()
            .await()

        // Delete subcollections in `users/{userId}`
        val userDocRef = db.collection("users").document(userId)

        // Delete all self-assessments
        val selfAssessmentsDocs = userDocRef.collection("selfAssessments").get().await()
        selfAssessmentsDocs.documents.forEach { it.reference.delete().await() }

        // Delete all video feedback
        val videoFeedbackDocs = userDocRef.collection("videoFeedback").get().await()
        videoFeedbackDocs.documents.forEach { it.reference.delete().await() }

        // Delete the user document itself
        userDocRef.delete().await()

        // Delete voice notes from Realtime Database
        rtdb.child("voice_notes").child(userId).removeValue().await()
    }
}