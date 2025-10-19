package com.example.tryggakampus.presentation.habitTracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.R
import com.example.tryggakampus.domain.model.Habit
import com.example.tryggakampus.util.DateUtils

@Composable
fun WeeklyProgressView(viewModel: HabitTrackerViewModel) {
    val habits by viewModel.habits.collectAsState()
    val weeklyProgress by viewModel.weeklyProgress.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadWeeklyProgress()
    }

    val weekOfYear = DateUtils.getWeekOfYear(selectedDate)
    val weekDays = stringArrayResource(id = R.array.weekdays)

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            stringResource(R.string.weekly_progress, weekOfYear),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column {
            habits.forEach { habit ->
                val progressForHabit: List<Boolean> = weeklyProgress[habit.id] ?: emptyList()
                HabitProgressRow(
                    habit = habit,
                    progress = progressForHabit,
                    weekDays = weekDays
                )
            }
        }
    }
}

@Composable
fun HabitProgressRow(habit: Habit, progress: List<Boolean>, weekDays: Array<String>) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(habit.title, style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                progress.forEachIndexed { index, completed ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (completed) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Completed", tint = Color.Green)
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Incomplete", tint = Color.Gray)
                        }
                        if (weekDays.indices.contains(index)) {
                            Text(weekDays[index])
                        }
                    }
                }
            }
        }
    }
}