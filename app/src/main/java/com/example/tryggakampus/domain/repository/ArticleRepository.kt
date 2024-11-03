package com.example.tryggakampus.domain.repository

import android.util.Log
import com.example.tryggakampus.domain.model.ArticleModel
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface ArticleRepository {
    suspend fun getAllArticles(source: Source): List<ArticleModel>
    suspend fun addArticle(article: ArticleModel)
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

    override suspend fun addArticle(article: ArticleModel) {
        try {
            Firebase.firestore
                .collection(COLLECTION_NAME)
                .document(article.id)
                .set(article)
                .await()
        } catch (e: Exception) {
            Log.d("AddArticleError", "Failed to add article: ${e.localizedMessage}")
            throw e  // Rethrow to allow the ViewModel to handle the error
        }
    }
}