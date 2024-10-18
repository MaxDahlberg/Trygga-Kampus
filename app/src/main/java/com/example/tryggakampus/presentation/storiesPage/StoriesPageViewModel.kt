package com.example.tryggakampus.presentation.storiesPage

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.dataStore
import com.example.tryggakampus.domain.model.StoryModel
import com.example.tryggakampus.domain.repository.StoryRepositoryImpl
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class StoriesPageViewModel: ViewModel() {
    var stories = mutableStateListOf<StoryModel>()
        private set

    fun loadStories(context: Context) {
        viewModelScope.launch {
            stories.clear()
            val lastFetchTimeKey = longPreferencesKey("stories_last_fetch_time")
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

            stories.addAll(StoryRepositoryImpl.getAllStories(source))

            if (source == Source.SERVER) {
                updateStoriesFetchTime(context)
            }
        }
    }

    private suspend fun updateStoriesFetchTime(context: Context) {
        val lastFetchTimeKey = longPreferencesKey("stories_last_fetch_time")

        context.dataStore.edit { settings ->
            settings[lastFetchTimeKey] = System.currentTimeMillis()
        }
    }
}