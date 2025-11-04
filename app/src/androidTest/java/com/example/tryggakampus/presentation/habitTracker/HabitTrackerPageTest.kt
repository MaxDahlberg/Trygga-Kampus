package com.example.tryggakampus.presentation.habitTracker

import com.example.tryggakampus.domain.model.Habit
import com.example.tryggakampus.domain.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

@Suppress("UNCHECKED_CAST", "Unused")
class HabitTrackerPageTest(
    private val mockRepo: HabitRepository = TestHabitRepository()
) {
    val viewModel: HabitTrackerViewModel = HabitTrackerViewModel(mockRepo)

    fun setHabitsForTest(habits: List<Habit>) {
        val field = viewModel.javaClass.getDeclaredField("_habits").apply { isAccessible = true }
        val state = field.get(viewModel) as MutableStateFlow<List<Habit>>
        state.value = habits
    }

    fun setCompletionsForTest(completions: Map<String, Boolean>) {
        val field = viewModel.javaClass.getDeclaredField("_completions").apply { isAccessible = true }
        val state = field.get(viewModel) as MutableStateFlow<Map<String, Boolean>>
        state.value = completions
    }

    fun setSelectedDateForTest(date: Date) {
        val field = viewModel.javaClass.getDeclaredField("_selectedDate").apply { isAccessible = true }
        val state = field.get(viewModel) as MutableStateFlow<Date>
        state.value = date

        val method = viewModel.javaClass.getDeclaredMethod("loadCompletionsForDate", Date::class.java).apply { isAccessible = true }
        method.invoke(viewModel, date)
    }

    // Toggle completion in the private _completions state
    fun toggleHabitCompletion(habitId: String) {
        val field = viewModel.javaClass.getDeclaredField("_completions").apply { isAccessible = true }
        val state = field.get(viewModel) as MutableStateFlow<Map<String, Boolean>>
        val current = state.value.toMutableMap()
        current[habitId] = !(current[habitId] ?: false)
        state.value = current
    }

    // Add a habit to the private _habits state
    fun addHabit(title: String, description: String) {
        val field = viewModel.javaClass.getDeclaredField("_habits").apply { isAccessible = true }
        val state = field.get(viewModel) as MutableStateFlow<List<Habit>>
        val newHabit = Habit(
            id = "test-${System.currentTimeMillis()}",
            title = title,
            description = description,
            color = "#FFFFFF",
            createdAt = Date().time
        )
        state.value += newHabit
    }

    // Remove a habit from the private _habits state
    fun deleteHabit(habitId: String) {
        val field = viewModel.javaClass.getDeclaredField("_habits").apply { isAccessible = true }
        val state = field.get(viewModel) as MutableStateFlow<List<Habit>>
        state.value = state.value.filterNot { it.id == habitId }
    }
}
