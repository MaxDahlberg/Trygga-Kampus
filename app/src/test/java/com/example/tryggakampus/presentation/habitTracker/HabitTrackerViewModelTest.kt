package com.example.tryggakampus.presentation.habitTracker

import com.example.tryggakampus.domain.model.Habit
import com.example.tryggakampus.domain.model.HabitCompletion
import com.example.tryggakampus.domain.repository.HabitRepository
import com.example.tryggakampus.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import java.util.*

@Suppress("UNCHECKED_CAST", "Unused")

@ExperimentalCoroutinesApi
class HabitTrackerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Simple in-memory fake repository
    private class FakeHabitRepository : HabitRepository {
        val habits = mutableListOf<Habit>()
        val completions = mutableListOf<HabitCompletion>()
        override suspend fun getHabits(): List<Habit> = habits.toList()
        override suspend fun addHabit(habit: Habit) { habits.add(habit) }
        override suspend fun updateHabit(habit: Habit) {
            val idx = habits.indexOfFirst { it.id == habit.id }
            if (idx >= 0) habits[idx] = habit
        }
        override suspend fun deleteHabit(habitId: String) { habits.removeAll { it.id == habitId } }
        override suspend fun getCompletions(habitId: String, startDate: Date, endDate: Date): List<HabitCompletion> =
            completions.filter { it.habitId == habitId && it.date in startDate.time..endDate.time }
        override suspend fun markCompletion(completion: HabitCompletion) { completions.add(completion) }
        override suspend fun unmarkCompletion(habitId: String, date: Date) {
            completions.removeAll { it.habitId == habitId && it.date == date.time }
        }
    }

    private lateinit var repo: FakeHabitRepository
    private lateinit var viewModel: HabitTrackerViewModel
    private lateinit var today: Date

    @Before
    fun setup() {
        repo = FakeHabitRepository()
        viewModel = HabitTrackerViewModel(repo)
        today = Date()
    }

    // Reflection helpers to mutate private StateFlows in the ViewModel for unit tests
    private fun <T> setPrivateState(vm: Any, fieldName: String, value: T) {
        val field = vm.javaClass.getDeclaredField(fieldName).apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val state = field.get(vm) as MutableStateFlow<T>
        state.value = value
    }

    private fun setHabitsForTest(vm: HabitTrackerViewModel, habits: List<Habit>) =
        setPrivateState(vm, "_habits", habits)

    private fun setCompletionsForTest(vm: HabitTrackerViewModel, completions: Map<String, Boolean>) =
        setPrivateState(vm, "_completions", completions)

    @Test
    fun initLoadsHabitsAndCompletionsForCurrentDate() = runTest {
        val fakeHabits = listOf(Habit(id = "1", title = "Test", description = "", color = "", createdAt = 0L))
        repo.habits.addAll(fakeHabits)

        // Start collecting dailyProgress so the stateIn flow is active
        val job = launch { viewModel.dailyProgress.collect { /* no-op */ } }

        viewModel.loadHabits()
        advanceUntilIdle()

        assertEquals(fakeHabits, viewModel.habits.value)
        assertEquals(mapOf("1" to false), viewModel.completions.value)
        job.cancel()
    }

    @Test
    fun addHabitCallsRepoAndReloadsHabits() = runTest {
        viewModel.addHabit("New Habit", "Desc")
        advanceUntilIdle()
        assertEquals(1, viewModel.habits.value.size)
        assertEquals("New Habit", viewModel.habits.value.first().title)
    }

    @Test
    fun toggleHabitCompletionMarksUnmarksAndReloadsCompletions() = runTest {
        val habit = Habit(id = "1", title = "Test", description = "", color = "", createdAt = today.time)
        repo.habits.add(habit)
        viewModel.loadHabits()
        advanceUntilIdle()

        viewModel.toggleHabitCompletion("1")
        advanceUntilIdle()
        assertTrue(viewModel.completions.value["1"] == true)

        viewModel.toggleHabitCompletion("1")
        advanceUntilIdle()
        assertTrue(viewModel.completions.value["1"] == false)
    }

    @Test
    fun deleteHabitCallsRepoAndReloadsHabits() = runTest {
        val habit = Habit(id = "1", title = "Test", description = "", color = "", createdAt = today.time)
        repo.habits.add(habit)
        viewModel.loadHabits()
        advanceUntilIdle()

        viewModel.deleteHabit("1")
        advanceUntilIdle()
        assertEquals(0, viewModel.habits.value.size)
    }

    @Test
    fun setSelectedDateUpdatesDateAndReloadsCompletionsWeeklyProgress() = runTest {
        val newDate = Date(today.time + 86_400_000)
        viewModel.setSelectedDate(newDate)
        advanceUntilIdle()
        assertEquals(newDate, viewModel.selectedDate.value)
    }

    @Test
    fun loadWeeklyProgressComputesWeeklyCompletionsForHabits() = runTest {
        val habit = Habit(id = "1", title = "H1", description = "", color = "", createdAt = today.time)
        repo.habits.add(habit)
        viewModel.loadHabits()
        advanceUntilIdle()

        // Mark completion today
        repo.markCompletion(HabitCompletion(id = "c1", habitId = "1", date = viewModel.selectedDate.value.time, completed = true))
        viewModel.loadWeeklyProgress()
        advanceUntilIdle()

        assertTrue(viewModel.weeklyProgress.value["1"]?.isNotEmpty() == true)
    }

    @Test
    fun dailyProgressComputesCorrectPercentageFromCompletions() = runTest {
        val job = launch { viewModel.dailyProgress.collect { } }
        // Two habits
        repo.habits.addAll(listOf(
            Habit(id = "1", title = "H1", description = "", color = "", createdAt = 0L),
            Habit(id = "2", title = "H2", description = "", color = "", createdAt = 0L)
        ))
        viewModel.loadHabits()
        advanceUntilIdle()

        // Mark completion for first habit only
        viewModel.toggleHabitCompletion("1")
        advanceUntilIdle()

        assertEquals(0.5f, viewModel.dailyProgress.value)
        job.cancel()
    }

    @Test
    fun dailyProgressIs0WhenNoHabits() = runTest {
        val job = launch { viewModel.dailyProgress.collect { } }
        setHabitsForTest(viewModel, emptyList())
        advanceUntilIdle()
        assertEquals(0f, viewModel.dailyProgress.value)
        job.cancel()
    }

}
