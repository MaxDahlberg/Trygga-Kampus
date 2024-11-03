package com.example.tryggakampus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ArticleModel(
    val id: String = "rad3edfeom",
    val title: String? = null,
    val summary: String = "No summary",
    val webpage: String? = null,
)
