package com.example.tryggakampus.presentation.habitTracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.domain.model.Habit
import com.example.tryggakampus.domain.model.HabitCompletion
import com.example.tryggakampus.domain.repository.HabitRepository
import com.example.tryggakampus.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class HabitTrackerViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date()) // Changed from LocalDate.now() to Date()
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow() // Changed to Date

    private val _completions = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val completions: StateFlow<Map<String, Boolean>> = _completions.asStateFlow()

    init {
        loadHabits()
    }

    fun setSelectedDate(date: Date) { // Changed from LocalDate to Date
        _selectedDate.value = date
        loadCompletionsForDate(date)
    }

    fun loadHabits() {
        viewModelScope.launch {
            _habits.value = habitRepository.getHabits()
            loadCompletionsForDate(_selectedDate.value)
        }
    }

    fun addHabit(title: String, description: String, color: String = "#2196F3") {
        viewModelScope.launch {
            val habit = Habit(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                color = color
            )
            habitRepository.addHabit(habit)
            loadHabits()
        }
    }

    fun toggleHabitCompletion(habitId: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val isCompleted = _completions.value[habitId] == true

            if (isCompleted) {
                habitRepository.unmarkCompletion(habitId, date)
            } else {
                val completion = HabitCompletion(
                    id = UUID.randomUUID().toString(),
                    habitId = habitId,
                    date = date.time, // Use .time to get Long timestamp
                    completed = true
                )
                habitRepository.markCompletion(completion)
            }
            loadCompletionsForDate(date)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
            loadHabits()
        }
    }

    private fun loadCompletionsForDate(date: Date) { // Changed from LocalDate to Date
        viewModelScope.launch {
            val completionsMap = mutableMapOf<String, Boolean>()
            _habits.value.forEach { habit ->
                val startOfDay = DateUtils.getStartOfDay(date)
                val endOfDay = DateUtils.getEndOfDay(date)
                val completions = habitRepository.getCompletions(habit.id, startOfDay, endOfDay)
                completionsMap[habit.id] = completions.any { it.completed }
            }
            _completions.value = completionsMap
        }
    }
}