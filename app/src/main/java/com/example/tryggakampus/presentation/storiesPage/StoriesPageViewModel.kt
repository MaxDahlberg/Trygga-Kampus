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
import com.example.tryggakampus.domain.repository.StoryCommentRepositoryImpl
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

            try {
                val commentsDeleted = StoryCommentRepositoryImpl.deleteCommentsForStory(story.id)
                if (!commentsDeleted) {
                    Log.w("StoriesVM", "Some comments might not have been deleted for story: ${story.id}")
                }

                val success = StoryRepositoryImpl.deleteStory(story.id)
                if (success) {
                    stories.removeAll { it.id == story.id }
                } else {
                    Log.e("StoriesVM", "Failed to delete story: ${story.id}")
                }
            } catch (e: Exception) {
                Log.e("StoriesVM", "Error deleting story ${story.id}: ${e.stackTraceToString()}")
            }
        }
    }

    // Comment state
    var comments = mutableStateListOf<StoryCommentModel>()
        private set

    var commentText = mutableStateOf(TextFieldValue(""))
        private set

    var commentAnonymity = mutableStateOf(true)
        private set

    var commentError = mutableStateOf<String?>(null)
        private set

    fun clearCommentError() {
        commentError.value = null
    }

    fun setCommentText(value: TextFieldValue) {
        commentText.value = value
    }

    fun setCommentAnonymity(value: Boolean) {
        commentAnonymity.value = value
    }

    fun loadComments(storyId: String) {
        viewModelScope.launch {
            try {
                val rawComments = StoryCommentRepositoryImpl.getCommentsForStory(storyId, Source.DEFAULT)
                val loadedComments = rawComments.map { data ->
                    StoryCommentModel(
                        id = data["id"] as? String ?: "",
                        storyId = data["storyId"] as? String ?: "",
                        userId = data["userId"] as? String ?: "",
                        author = data["author"] as? String,
                        content = data["content"] as? String ?: "",
                        anonymous = data["anonymous"] as? Boolean ?: false,
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                }

                val enrichedComments = loadedComments.map { comment ->
                    enrichCommentWithUsername(comment)
                }

                comments.clear()
                comments.addAll(enrichedComments)
                clearCommentError()
            } catch (e: Exception) {
                commentError.value = "Failed to load comments. Please try again."
            }
        }
    }

    fun postComment(storyId: String) {
        viewModelScope.launch {
            val text = commentText.value.text.trim()
            if (text.isEmpty()) return@launch

            try {
                val comment = StoryCommentRepositoryImpl.postComment(
                    storyId = storyId,
                    content = text,
                    isAnonymous = commentAnonymity.value
                )
                comment?.let {
                    val enriched = enrichCommentWithUsername(it)
                    comments.add(0, enriched)
                }
                commentText.value = TextFieldValue("")
                setCommentAnonymity(true)
                clearCommentError()
            } catch (e: Exception) {
                commentError.value = "Failed to post comment. Please try again."
            }
        }
    }

    fun deleteComment(comment: StoryCommentModel) {
        viewModelScope.launch {
            try {
                if (comment.id.isEmpty()) return@launch

                val success = StoryCommentRepositoryImpl.deleteComment(comment.id)
                if (success) {
                    comments.removeAll { it.id == comment.id }
                    clearCommentError()
                } else {
                    commentError.value = "Could not delete comment."
                }
            } catch (e: Exception) {
                commentError.value = "Error deleting comment. Please try again."
            }
        }
    }

    private suspend fun enrichCommentWithUsername(comment: StoryCommentModel): StoryCommentModel {
        if (comment.anonymous) return comment.copy(author = "Anonymous")

        var (_, userInfo) = UserInformationRepositoryImpl.getUserInformation(comment.userId, Source.SERVER)

        if (userInfo == null) {
            val (_, cachedUserInfo) = UserInformationRepositoryImpl.getUserInformation(comment.userId, Source.CACHE)
            userInfo = cachedUserInfo
        }

        val username = userInfo?.username ?: "Unknown User"
        return comment.copy(author = username)
    }
}
