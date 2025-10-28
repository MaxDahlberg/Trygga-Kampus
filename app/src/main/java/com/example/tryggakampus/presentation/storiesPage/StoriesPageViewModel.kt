package com.example.tryggakampus.presentation.storiesPage

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.domain.model.StoryCommentModel
import com.example.tryggakampus.domain.model.StoryModel
import com.example.tryggakampus.domain.repository.StoryRepositoryImpl
import com.example.tryggakampus.domain.repository.UserInformationRepositoryImpl
import com.google.firebase.firestore.Source
import kotlinx.coroutines.launch

class StoriesPageViewModel : ViewModel() {
    // Story state
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
                val enriched = enrichStoryWithUsername(it)
                stories.add(0, enriched)
            }

            setStoryTitleValue(TextFieldValue())
            setStoryFormValue(TextFieldValue())
            setShowNewStoryForm(false)
            setStoryAnonymity(true)
        }
    }

    private var lastFetchTimeMillis: Long = 0L

    fun loadStories(context: Context, dataSource: Source? = null) {
        viewModelScope.launch {
            setLoadingStories(true)

            val currentTimeMillis = System.currentTimeMillis()
            val timeDifference = (currentTimeMillis - lastFetchTimeMillis) / 1000
            val source = dataSource ?: if (timeDifference >= 20) Source.SERVER else Source.CACHE

            val fetchedStories = StoryRepositoryImpl.getAllStories(source)

            val enrichedStories = fetchedStories.map { story ->
                enrichStoryWithUsername(story)
            }

            stories.clear()
            stories.addAll(enrichedStories.distinctBy { it.id })

            if (source == Source.SERVER) {
                lastFetchTimeMillis = currentTimeMillis
            }

            setLoadingStories(false)
        }
    }

    private suspend fun enrichStoryWithUsername(story: StoryModel): StoryModel {
        if (story.anonymous) return story.copy(author = "Anonymous")

        var (_, userInfo) = UserInformationRepositoryImpl.getUserInformation(story.userId, Source.SERVER)

        if (userInfo == null) {
            val (_, cachedUserInfo) = UserInformationRepositoryImpl.getUserInformation(story.userId, Source.CACHE)
            userInfo = cachedUserInfo
        }

        val username = userInfo?.username ?: "Unknown User"
        return story.copy(author = username)
    }

    fun deleteStory(story: StoryModel) {
        viewModelScope.launch {
            if (story.id.isEmpty()) return@launch

            val success = StoryRepositoryImpl.deleteStory(story.id)
            if (success) {
                stories.removeAll { it.id == story.id }
                Log.d("StoriesVM", "Deleted story locally: ${story.id}")
            } else {
                Log.d("StoriesVM", "Failed to delete story: ${story.id}")
            }
        }
    }

    // Comment state
    var comments = mutableStateListOf<StoryCommentModel>()
        private set

    var commentText = mutableStateOf(TextFieldValue(""))
        private set

    var commentAnonymity = mutableStateOf(false)
        private set

    fun setCommentText(value: TextFieldValue) {
        commentText.value = value
    }

    fun setCommentAnonymity(value: Boolean) {
        commentAnonymity.value = value
    }

    fun loadComments(storyId: String) {
        // todo
    }

    fun postComment(storyId: String) {
        // todo
    }

    fun deleteComment(comment: StoryCommentModel) {
        // todo
    }
}
