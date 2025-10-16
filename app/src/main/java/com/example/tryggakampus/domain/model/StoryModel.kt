package com.example.tryggakampus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StoryModel(
    var id: String = "",
    val title: String? = null,
    val userId: String? = null,
    val author: String? = null,
    val content: String = "No content",
    val commentIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
