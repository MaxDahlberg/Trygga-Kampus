package com.example.tryggakampus.presentation.storiesPage

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

import androidx.compose.ui.text.input.TextFieldValue

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

    var showNewStoryForm = mutableStateOf<Boolean>(false)
        private set

    var storyFormValue = mutableStateOf(TextFieldValue(""))
    var storyTitleValue = mutableStateOf(TextFieldValue(""))
    var storyAnonymity = mutableStateOf<Boolean>(true)
        private set

    var loadingStories = mutableStateOf(false)
        private set

    private fun setLoadingStories(b: Boolean) {
        loadingStories.value = b
    }

    fun setShowNewStoryForm(b: Boolean) {
        showNewStoryForm.value = b
    }

    fun setStoryFormValue(textFieldValue: TextFieldValue) {
        storyFormValue.value = textFieldValue
    }

    fun setStoryTitleValue(textFieldValue: TextFieldValue) {
        storyTitleValue.value = textFieldValue
    }

    fun setStoryAnonymity(b: Boolean) {
        storyAnonymity.value = b
    }

    fun submitStory() {
        viewModelScope.launch {
            val story = StoryRepositoryImpl.postStory(
                title = storyTitleValue.value.text,
                content = storyFormValue.value.text,
                isAnonymous = storyAnonymity.value
            )

            story?.let {
                stories.add(0, story)
            }

            setStoryTitleValue(TextFieldValue())
            setStoryFormValue(TextFieldValue())
            setShowNewStoryForm(false)
            setStoryAnonymity(true)
        }
    }

    fun loadStories(context: Context, dataSource: Source? = null) {
        viewModelScope.launch {
            setLoadingStories(true)

            stories.clear()
            val lastFetchTimeKey = longPreferencesKey("stories_last_fetch_time")
            val lastFetchTime: Long = context.dataStore.data
                .map { preferences -> preferences[lastFetchTimeKey] ?: 0L }
                .first()

            val currentTimeMillis = System.currentTimeMillis()
            val timeDifference = (currentTimeMillis - lastFetchTime) / 1000
            val source = dataSource ?: if (timeDifference >= 20) {
                Source.SERVER
            } else {
                Source.CACHE
            }

            stories.addAll(StoryRepositoryImpl.getAllStories(source))
            Log.d("STORIES", stories.toList().toString())

            if (source == Source.SERVER) {
                updateStoriesFetchTime(context)
            }

            setLoadingStories(false)
        }
    }

    private suspend fun updateStoriesFetchTime(context: Context) {
        val lastFetchTimeKey = longPreferencesKey("stories_last_fetch_time")

        context.dataStore.edit { settings ->
            settings[lastFetchTimeKey] = System.currentTimeMillis()
        }
    }
}