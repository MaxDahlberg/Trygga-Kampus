package com.example.tryggakampus.domain.repository

import android.util.Log
import com.example.tryggakampus.domain.model.StoryCommentModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

interface StoryCommentRepository {
    suspend fun getCommentsForStory(storyId: String, source: Source): List<Map<String, Any>>
    suspend fun postComment(
        storyId: String,
        content: String,
        isAnonymous: Boolean = true
    ): StoryCommentModel?
    suspend fun deleteComment(commentId: String): Boolean
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
    override suspend fun postComment(
        storyId: String,
        content: String,
        isAnonymous: Boolean
    ): StoryCommentModel? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.d("StoryCommentRepo", "No signed-in user")
            return null
        }

        return try {
            val collection = Firebase.firestore.collection(COLLECTION_NAME)
            val docRef = collection.document()

            val comment = StoryCommentModel(
                id = docRef.id,
                storyId = storyId,
                userId = currentUser.uid,
                content = content,
                anonymous = isAnonymous,
                author = null,
                createdAt = System.currentTimeMillis()
            )

            docRef.set(comment).await()
            comment
        } catch (e: Exception) {
            Log.d("StoryCommentRepo", "Error posting comment: ${e.stackTraceToString()}")
            null
        }
    }

    override suspend fun deleteComment(commentId: String): Boolean {
        return try {
            Firebase.firestore
                .collection(COLLECTION_NAME)
                .document(commentId)
                .delete()
                .await()
            Log.d("StoryCommentRepo", "Deleted comment: $commentId")
            true
        } catch (e: Exception) {
            Log.d("StoryCommentRepo", "Error deleting comment: ${e.stackTraceToString()}")
            false
        }
    }
}