package com.example.tryggakampus.domain.model

import com.google.firebase.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class StoryModel(
    var id: String? = null,
    val title: String? = null,
    // val userId: String,
    val author: String? = null,
    val content: String = "No content",
    // val createdAt: Timestamp = Timestamp.now()
)
