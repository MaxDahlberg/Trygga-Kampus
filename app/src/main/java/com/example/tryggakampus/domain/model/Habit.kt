package com.example.tryggakampus.domain.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val color: String,
    val createdAt: Long = Date().time,
    val isActive: Boolean = true
)

@Serializable
data class HabitCompletion(
    val id: String,
    val habitId: String,
    val date: Long, // This should be Long, not LocalDate
    val completed: Boolean = false
)