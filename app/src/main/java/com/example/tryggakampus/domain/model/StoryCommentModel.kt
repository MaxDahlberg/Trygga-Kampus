package com.example.tryggakampus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StoryCommentModel(
    var id: String = "",
    var storyId: String= "",
    val userId: String= "",
    val author: String? = null,
    val content: String = "No content",
    val anonymous: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)