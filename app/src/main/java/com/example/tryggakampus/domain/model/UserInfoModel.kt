package com.example.tryggakampus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoModel(
    val userId: String = "",
    val username: String? = null,
    val hobbies: List<String> = emptyList()
)