package com.example.tryggakampus.presentation.habitTracker

import com.example.tryggakampus.domain.model.Habit
import com.example.tryggakampus.domain.model.HabitCompletion
import com.example.tryggakampus.domain.repository.HabitRepository
import com.example.tryggakampus.util.DateUtils
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

@Suppress("UNCHECKED_CAST", "Unused")

@ExperimentalCoroutinesApi
class HabitTrackerViewModelTest {

    private val mockRepo = mockk<HabitRepository>(relaxed = true)
    private val viewModel = HabitTrackerViewModel(mockRepo)
    private val mockDate = Date()

    @Before
    fun setup() {
        mockkStatic(DateUtils::class)
        every { DateUtils.getStartOfDay(any()) } returns mockDate
        every { DateUtils.getEndOfDay(any()) } returns mockDate
    }

    @After
    fun teardown() {
        unmockkAll()
        unmockkStatic(DateUtils::class)
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
        coEvery { mockRepo.getHabits() } returns fakeHabits
        coEvery { mockRepo.getCompletions(any(), any(), any()) } returns emptyList<HabitCompletion>()

        viewModel.loadHabits()

        assertEquals(fakeHabits, viewModel.habits.value)
        assertEquals(emptyMap<String, Boolean>(), viewModel.completions.value)
    }

    @Test
    fun addHabitCallsRepoAndReloadsHabits() = runTest {
        val fakeHabits = listOf(Habit(id = "1", title = "New Habit", description = "Desc", color = "#FFC107", createdAt = Date().time))
        coEvery { mockRepo.addHabit(any()) } just Runs
        coEvery { mockRepo.getHabits() } returns fakeHabits

        viewModel.addHabit("New Habit", "Desc")

        coVerify { mockRepo.addHabit(any()) }
        coVerify { mockRepo.getHabits() }
        assertEquals(fakeHabits, viewModel.habits.value)
    }

    @Test
    fun toggleHabitCompletionMarksUnmarksAndReloadsCompletions() = runTest {
        val mockCompletion = HabitCompletion(id = "c1", habitId = "1", date = mockDate.time, completed = true)
        coEvery { mockRepo.getCompletions(any(), any(), any()) } returns listOf(mockCompletion)
        coEvery { mockRepo.markCompletion(any()) } just Runs
        coEvery { mockRepo.unmarkCompletion(any(), any()) } just Runs

        viewModel.toggleHabitCompletion("1")

        coVerify { mockRepo.markCompletion(any()) }
        coVerify { mockRepo.getCompletions(any(), any(), any()) }

        viewModel.toggleHabitCompletion("1")

        coVerify { mockRepo.unmarkCompletion("1", mockDate) }
    }

    @Test
    fun deleteHabitCallsRepoAndReloadsHabits() = runTest {
        val fakeHabits = emptyList<Habit>()
        coEvery { mockRepo.deleteHabit(any()) } just Runs
        coEvery { mockRepo.getHabits() } returns fakeHabits

        viewModel.deleteHabit("1")

        coVerify { mockRepo.deleteHabit("1") }
        coVerify { mockRepo.getHabits() }
        assertEquals(fakeHabits, viewModel.habits.value)
    }

    @Test
    fun setSelectedDateUpdatesDateAndReloadsCompletionsWeeklyProgress() = runTest {
        val newDate = Date(mockDate.time + 86400000)  // Tomorrow
        coEvery { mockRepo.getCompletions(any(), any(), any()) } returns emptyList<HabitCompletion>()

        viewModel.setSelectedDate(newDate)

        assertEquals(newDate, viewModel.selectedDate.value)
        coVerify { mockRepo.getCompletions(any(), any(), any()) }  // Called for new date
    }

    @Test
    fun loadWeeklyProgressComputesWeeklyCompletionsForHabits() = runTest {
        val mockWeekDates = listOf(mockDate, Date(mockDate.time + 86400000))  // 2 days
        val mockCompletions = listOf(HabitCompletion(id = "c1", habitId = "1", date = mockDate.time, completed = true))
        coEvery { mockRepo.getCompletions(any(), any(), any()) } returns mockCompletions

        viewModel.loadWeeklyProgress()

        assertTrue(viewModel.weeklyProgress.value.isNotEmpty())
        coVerify(exactly = 2) { mockRepo.getCompletions(any(), any(), any()) }  // Called for each day
    }

    @Test
    fun dailyProgressComputesCorrectPercentageFromCompletions() = runTest {
        setHabitsForTest(viewModel, listOf(
            Habit(id = "1", title = "H1", description = "", color = "", createdAt = 0L),
            Habit(id = "2", title = "H2", description = "", color = "", createdAt = 0L)
        ))
        setCompletionsForTest(viewModel, mapOf("1" to true, "2" to false))

        assertEquals(0.5f, viewModel.dailyProgress.value)
    }

    @Test
    fun dailyProgressIs0WhenNoHabits() = runTest {
        setHabitsForTest(viewModel, emptyList())

        assertEquals(0f, viewModel.dailyProgress.value)
    }

    @Test
    fun dailyProgressIs1WhenAllHabitsCompleted() = runTest {
        setHabitsForTest(viewModel, listOf(Habit(id = "1", title = "H1", description = "", color = "", createdAt = 0L)))
        setCompletionsForTest(viewModel, mapOf("1" to true))

        assertEquals(1f, viewModel.dailyProgress.value)
    }
}
