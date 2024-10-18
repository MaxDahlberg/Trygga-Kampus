package com.example.tryggakampus.domain.repository

import android.util.Log
import com.example.tryggakampus.domain.model.ArticleModel
import com.google.firebase.firestore.Source

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface ArticleRepository {
    suspend fun getAllArticles(source: Source): List<ArticleModel>
}

object ArticleRepositoryImpl: ArticleRepository {
    private const val COLLECTION_NAME = "articles"

    override suspend fun getAllArticles(source: Source): List<ArticleModel> {
        try {
            val result = Firebase.firestore
                .collection(COLLECTION_NAME)
                .get(source)
                .await()

            return result.map { document ->
                document.toObject(ArticleModel::class.java)
            }
        } catch (e: Exception) {
            Log.d("FATAL", e.stackTraceToString())
            return emptyList()
        }
    }
}