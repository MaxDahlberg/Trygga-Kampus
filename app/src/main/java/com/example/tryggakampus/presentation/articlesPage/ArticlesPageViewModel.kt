package com.example.tryggakampus.presentation.articlesPage

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.dataStore
import com.example.tryggakampus.domain.model.ArticleModel
import com.example.tryggakampus.domain.repository.ArticleRepositoryImpl
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ArticlesPageViewModel: ViewModel() {
    var articles = mutableStateListOf<ArticleModel>()
        private set

    fun loadArticles(context: Context) {
        viewModelScope.launch {
            articles.clear()
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

            articles.addAll(ArticleRepositoryImpl.getAllArticles(source))

            if (source == Source.SERVER) {
                updateArticlesFetchTime(context)
            }
        }
    }

    private suspend fun updateArticlesFetchTime(context: Context) {
        val lastFetchTimeKey = longPreferencesKey("articles_last_fetch_time")

        context.dataStore.edit { settings ->
            settings[lastFetchTimeKey] = System.currentTimeMillis()
        }
    }
}