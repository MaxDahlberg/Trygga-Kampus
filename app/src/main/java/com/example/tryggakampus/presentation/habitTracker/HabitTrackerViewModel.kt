package com.example.tryggakampus.presentation.habitTracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.domain.model.Habit
import com.example.tryggakampus.domain.model.HabitCompletion
import com.example.tryggakampus.domain.repository.HabitRepository
import com.example.tryggakampus.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class HabitTrackerViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _completions = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val completions: StateFlow<Map<String, Boolean>> = _completions.asStateFlow()

    private val _weeklyProgress = MutableStateFlow<Map<String, List<Boolean>>>(emptyMap())
    val weeklyProgress: StateFlow<Map<String, List<Boolean>>> = _weeklyProgress.asStateFlow()

    val dailyProgress: StateFlow<Float> = _completions.map {
        if (it.isEmpty()) 0f else it.values.count { it } / it.size.toFloat()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    private val habitColors = listOf(
        "#FFC107", "#FF5722", "#E91E63", "#9C27B0", "#3F51B5",
        "#03A9F4", "#009688", "#8BC34A", "#CDDC39", "#FF9800"
    )

    init {
        loadHabits()
    }

    fun loadWeeklyProgress() {
        viewModelScope.launch {
            val weeklyProgressMap = mutableMapOf<String, List<Boolean>>()
            val calendar = Calendar.getInstance().apply {
                time = _selectedDate.value
                firstDayOfWeek = Calendar.MONDAY
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }
            val weekDates = (0..6).map { calendar.time.also { calendar.add(Calendar.DAY_OF_YEAR, 1) } }

            _habits.value.forEach { habit ->
                val habitProgress = weekDates.map { date ->
                    val startOfDay = DateUtils.getStartOfDay(date)
                    val endOfDay = DateUtils.getEndOfDay(date)
                    habitRepository.getCompletions(habit.id, startOfDay, endOfDay).any { it.completed }
                }
                weeklyProgressMap[habit.id] = habitProgress
            }
            _weeklyProgress.value = weeklyProgressMap
        }
    }

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
        loadCompletionsForDate(date)
        loadWeeklyProgress()
    }

    fun loadHabits() {
        viewModelScope.launch {
            _habits.value = habitRepository.getHabits()
            loadCompletionsForDate(_selectedDate.value)
        }
    }

    fun addHabit(title: String, description: String) {
        viewModelScope.launch {
            val habit = Habit(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                color = habitColors.random(),
                createdAt = Date().time
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
                habitRepository.markCompletion(HabitCompletion(id = UUID.randomUUID().toString(), habitId = habitId, date = date.time, completed = true))
            }
            loadCompletionsForDate(date)
            loadWeeklyProgress() // Reload weekly progress as well
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
            loadHabits()
        }
    }

    private fun loadCompletionsForDate(date: Date) {
        viewModelScope.launch {
            val completionsMap = mutableMapOf<String, Boolean>()
            _habits.value.forEach { habit ->
                val startOfDay = DateUtils.getStartOfDay(date)
                val endOfDay = DateUtils.getEndOfDay(date)
                completionsMap[habit.id] = habitRepository.getCompletions(habit.id, startOfDay, endOfDay).any { it.completed }
            }
            _completions.value = completionsMap
        }
    }
}