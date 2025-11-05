package com.example.tryggakampus.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.tryggakampus.domain.model.Habit
import com.example.tryggakampus.domain.model.HabitCompletion
import com.example.tryggakampus.domain.repository.HabitRepository
import com.example.tryggakampus.util.DateUtils
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class LocalHabitRepository(private val context: Context) : HabitRepository {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun getHabits(): List<Habit> {
        val habitsJson = sharedPreferences.getString("habits", "[]") ?: "[]"
        return try {
            json.decodeFromString(habitsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        habits.add(habit)
        saveHabits(habits)
    }

    override suspend fun updateHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
        }
    }

    override suspend fun deleteHabit(habitId: String) {
        val habits = getHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
        removeCompletionsForHabit(habitId)
    }

    override suspend fun getCompletions(habitId: String, startDate: Date, endDate: Date): List<HabitCompletion> {
        val key = "completions_$habitId"
        val completionsJson = sharedPreferences.getString(key, "[]") ?: "[]"
        val allCompletions: List<HabitCompletion> = try {
            json.decodeFromString(completionsJson)
        } catch (e: Exception) {
            emptyList()
        }

        return allCompletions.filter { completion ->
            val completionDate = Date(completion.date)
            completionDate.time >= startDate.time && completionDate.time <= endDate.time
        }
    }

    override suspend fun markCompletion(completion: HabitCompletion) {
        val key = "completions_${completion.habitId}"
        val allCompletions = getCompletionsForAllTime(completion.habitId).toMutableList()

        val existingIndex = allCompletions.indexOfFirst {
            DateUtils.isSameDay(Date(it.date), Date(completion.date))
        }

        if (existingIndex != -1) {
            allCompletions[existingIndex] = completion
        } else {
            allCompletions.add(completion)
        }

        sharedPreferences.edit()
            .putString(key, json.encodeToString(allCompletions))
            .apply()
    }

    override suspend fun unmarkCompletion(habitId: String, date: Date) {
        val key = "completions_$habitId"
        val allCompletions = getCompletionsForAllTime(habitId)
        val filteredCompletions = allCompletions.filterNot {
            DateUtils.isSameDay(Date(it.date), date)
        }

        sharedPreferences.edit()
            .putString(key, json.encodeToString(filteredCompletions))
            .apply()
    }

    private fun getCompletionsForAllTime(habitId: String): List<HabitCompletion> {
        val key = "completions_$habitId"
        val completionsJson = sharedPreferences.getString(key, "[]") ?: "[]"
        return try {
            json.decodeFromString(completionsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHabits(habits: List<Habit>) {
        sharedPreferences.edit()
            .putString("habits", json.encodeToString(habits))
            .apply()
    }

    private fun removeCompletionsForHabit(habitId: String) {
        sharedPreferences.edit()
            .remove("completions_$habitId")
            .apply()
    }
}