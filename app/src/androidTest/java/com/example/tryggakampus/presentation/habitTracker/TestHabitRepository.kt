package com.example.tryggakampus.presentation.habitTracker

import com.example.tryggakampus.domain.model.Habit
import com.example.tryggakampus.domain.model.HabitCompletion
import com.example.tryggakampus.domain.repository.HabitRepository
import java.util.Date

class TestHabitRepository : HabitRepository {
    override suspend fun getHabits(): List<Habit> = listOf(
        Habit(id = "1", title = "Test Habit 1", description = "Desc 1", color = "#FFC107", createdAt = Date().time)
    )

    override suspend fun getCompletions(habitId: String, startDate: Date, endDate: Date): List<HabitCompletion> = emptyList()

    override suspend fun addHabit(habit: Habit) = Unit

    override suspend fun updateHabit(habit: Habit) = Unit

    override suspend fun markCompletion(completion: HabitCompletion) = Unit

    override suspend fun unmarkCompletion(habitId: String, date: Date) = Unit

    override suspend fun deleteHabit(habitId: String) = Unit
}
