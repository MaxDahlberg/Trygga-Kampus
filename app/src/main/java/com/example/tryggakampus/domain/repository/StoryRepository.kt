package com.example.tryggakampus.domain.repository

import android.util.Log
import com.example.tryggakampus.domain.model.StoryModel

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface StoryRepository {
    suspend fun getAllStories(): List<StoryModel>
}

class StoryRepositoryImpl: StoryRepository {
    private val db = Firebase.firestore
    private val columnName = "student-stories"

    override suspend fun getAllStories(): List<StoryModel> {
        try {
            val result = db.collection(columnName).get().await()

            return result.map { document ->
                document.toObject(StoryModel::class.java)
            }
        } catch (e: Exception) {
            Log.d("FIRESTORE", "Failed to retrieve collection 'student-stories'")
            Log.d("FATAL", e.stackTraceToString())
            return emptyList()
        }
    }
}