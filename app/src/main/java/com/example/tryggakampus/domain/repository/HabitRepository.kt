package com.example.tryggakampus.domain.repository

import com.example.tryggakampus.domain.model.Habit
import com.example.tryggakampus.domain.model.HabitCompletion
import java.util.*

interface HabitRepository {
    suspend fun getHabits(): List<Habit>
    suspend fun addHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)
    suspend fun getCompletions(habitId: String, startDate: Date, endDate: Date): List<HabitCompletion>
    suspend fun markCompletion(completion: HabitCompletion)
    suspend fun unmarkCompletion(habitId: String, date: Date)
}