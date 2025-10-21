package com.example.tryggakampus.util

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class GdprUserDataHelper {

    private val db = Firebase.firestore


    suspend fun fetchUserData(userId: String) { /*todo*/ }


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
    }
}