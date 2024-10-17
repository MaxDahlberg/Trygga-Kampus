package com.example.tryggakampus.presentation.storiesPage

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.example.tryggakampus.domain.model.StoryModel
import com.example.tryggakampus.domain.repository.StoryRepository
import com.example.tryggakampus.domain.repository.StoryRepositoryImpl

class StoriesPageViewModel: ViewModel() {
    var stories = mutableStateListOf<StoryModel>()
        private set

    suspend fun loadStories() {
        stories.clear()
        stories.addAll(StoryRepositoryImpl().getAllStories())
        Log.d("LOAD", "stories: ${stories.size}")
    }
}