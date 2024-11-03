package com.example.tryggakampus.domain.model

enum class AdviceCategory {
    PREVENTION,
    SUPPORT,
    FAMILY
}

data class AdviceItem(
    val title: String,
    val text: String,
    val image: Int
)