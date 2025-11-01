package com.example.tryggakampus.domain.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

interface StoryCommentRepository {
    suspend fun getCommentsForStory(storyId: String, source: Source): List<Map<String, Any>>
    suspend fun postComment()
    suspend fun deleteComment()
}

object StoryCommentRepositoryImpl : StoryCommentRepository {
    private const val COLLECTION_NAME = "story-comments"

    override suspend fun getCommentsForStory(
        storyId: String,
        source: Source
    ): List<Map<String, Any>> {
        return try {
            val snapshot = Firebase.firestore
                .collection(COLLECTION_NAME)
                .whereEqualTo("storyId", storyId)
                .get(source)
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data
            }
        } catch (e: Exception) {
            Log.e("StoryCommentRepo", "Error fetching comments: ${e.stackTraceToString()}")
            emptyList()
        }
    }

    override suspend fun postComment() {}
    override suspend fun deleteComment() {}
}
