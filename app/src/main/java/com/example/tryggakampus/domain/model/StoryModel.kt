package com.example.tryggakampus.domain.model

import com.google.firebase.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class StoryModel(
    val id: String = "1ab23c4tj1",
    val title: String? = null,
    val author: String? = null,
    val content: String = "No content",
    // val createdAt: Timestamp = Timestamp.now()
)
