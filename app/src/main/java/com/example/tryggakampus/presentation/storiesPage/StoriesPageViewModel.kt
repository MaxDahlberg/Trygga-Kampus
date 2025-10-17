package com.example.tryggakampus.presentation.storiesPage

import android.content.Context
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
import com.example.tryggakampus.domain.repository.UserInformationRepositoryImpl

import com.google.firebase.firestore.Source

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class StoriesPageViewModel : ViewModel() {
    var stories = mutableStateListOf<StoryModel>()
        private set

    var showNewStoryForm = mutableStateOf(false)
        private set

    var storyFormValue = mutableStateOf(TextFieldValue(""))
    var storyTitleValue = mutableStateOf(TextFieldValue(""))
    var storyAnonymity = mutableStateOf(true)
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
                // enrich the new story before adding it
                val enriched = enrichStoryWithUsername(it)
                stories.add(0, enriched)
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

            val lastFetchTimeKey = longPreferencesKey("stories_last_fetch_time")
            val lastFetchTime: Long = context.dataStore.data
                .map { preferences -> preferences[lastFetchTimeKey] ?: 0L }
                .first()

            val currentTimeMillis = System.currentTimeMillis()
            val timeDifference = (currentTimeMillis - lastFetchTime) / 1000
            val source = dataSource ?: if (timeDifference >= 20) Source.SERVER else Source.CACHE

            // Fetch stories
            val fetchedStories = StoryRepositoryImpl.getAllStories(source)

            // Enrich with usernames
            val enrichedStories = fetchedStories.map { story ->
                enrichStoryWithUsername(story)
            }

            stories.clear()
            stories.addAll(enrichedStories.distinctBy { it.id })

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

    private suspend fun enrichStoryWithUsername(story: StoryModel): StoryModel {
        if (story.anonymous) return story.copy(author = "Anonymous")

        // Try SERVER first
        var (_, userInfo) = UserInformationRepositoryImpl.getUserInformation(story.userId, Source.SERVER)

        // Fallback to CACHE if server fails
        if (userInfo == null) {
            val (_, cachedUserInfo) = UserInformationRepositoryImpl.getUserInformation(story.userId, Source.CACHE)
            userInfo = cachedUserInfo
        }

        val username = userInfo?.username ?: "Unknown User"
        return story.copy(author = username)
    }
}