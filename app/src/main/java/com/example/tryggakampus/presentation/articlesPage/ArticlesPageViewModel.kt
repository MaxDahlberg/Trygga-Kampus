package com.example.tryggakampus.presentation.articlesPage

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.dataStore
import com.example.tryggakampus.domain.model.ArticleModel
import com.example.tryggakampus.domain.repository.ArticleRepository
import com.example.tryggakampus.domain.repository.ArticleRepositoryImpl
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ArticlesPageViewModel : ViewModel() {
    var articles = mutableStateListOf<ArticleModel>()
        private set
    var deleteMode by mutableStateOf(false)
        private set
    var loadingArticles = mutableStateOf(true)
        private set
    val errorMessage = MutableStateFlow<String?>(null)

    private fun setLoadingArticles(isLoading: Boolean) {
        loadingArticles.value = isLoading
    }

    fun loadArticles(context: Context) {
        viewModelScope.launch {
            setLoadingArticles(true)

            val lastFetchTimeKey = longPreferencesKey("articles_last_fetch_time")
            val lastFetchTime: Long = context.dataStore.data
                .map { preferences -> preferences[lastFetchTimeKey] ?: 0L }
                .first()

            val currentTimeMillis = System.currentTimeMillis()
            val timeDifference = (currentTimeMillis - lastFetchTime) / 1000
            val source = if (timeDifference >= 20) {
                Source.SERVER
            } else {
                Source.CACHE
            }

            if (source == Source.SERVER) {
                articles.clear()
            }


            val (result, fetchedArticles) = ArticleRepositoryImpl.getAllArticles(source)
            when (result) {
                ArticleRepository.RepositoryResult.SUCCESS -> {
                    articles.clear()
                    articles.addAll(fetchedArticles)
                    if (source == Source.SERVER) {
                        updateArticlesFetchTime(context)
                    }
                }
                ArticleRepository.RepositoryResult.ERROR_NETWORK -> {
                    errorMessage.value = "Network error occurred. Please check your connection."
                }
                ArticleRepository.RepositoryResult.ERROR_DATABASE -> {
                    errorMessage.value = "Database error. Could not fetch articles."
                }
                ArticleRepository.RepositoryResult.ERROR_UNKNOWN -> {
                    errorMessage.value = "An unexpected error occurred while loading articles."
                }
            }

            setLoadingArticles(false)
        }
    }

    private suspend fun updateArticlesFetchTime(context: Context) {
        val lastFetchTimeKey = longPreferencesKey("articles_last_fetch_time")
        context.dataStore.edit { settings ->
            settings[lastFetchTimeKey] = System.currentTimeMillis()
        }
    }

    fun addArticle(title: String, summary: String, webpage: String) {
        if (title.isBlank() || summary.isBlank() || webpage.isBlank()) {
            errorMessage.value = "Title, summary, and webpage cannot be empty."
            return
        }

        viewModelScope.launch {
            val (result, newArticle) = ArticleRepositoryImpl.addArticle(ArticleModel(
                title = title,
                summary = summary,
                webpage = webpage
            ))

            if (result != ArticleRepository.RepositoryResult.SUCCESS) {
                errorMessage.value = "Failed to add article. Please try again."
                return@launch
            }

            articles.add(0, newArticle!!)
        }
    }

    fun deleteArticle(article: ArticleModel) {
        viewModelScope.launch {
            val result = ArticleRepositoryImpl.deleteArticle(article.id)
            if (result != ArticleRepository.RepositoryResult.SUCCESS) {
                errorMessage.value = "Failed to delete article."
            }

            articles.removeIf { a -> a.id == article.id }
        }
    }

    fun toggleDeleteMode() {
        deleteMode = !deleteMode
    }

    fun clearErrorMessage() {
        errorMessage.value = null
    }
}
