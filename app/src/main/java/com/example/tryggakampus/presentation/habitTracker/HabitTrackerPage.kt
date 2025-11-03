package com.example.tryggakampus.presentation.habitTracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.R
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerPage(
    viewModel: HabitTrackerViewModel? = viewModel<HabitTrackerViewModel>(),
    onNavigateBack: () -> Unit
) {
    val vm: HabitTrackerViewModel = viewModel ?: viewModel<HabitTrackerViewModel>()
    val habits by vm.habits.collectAsState()
    val selectedDate by vm.selectedDate.collectAsState()
    val completions by vm.completions.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }

    val currentLocale = LocalConfiguration.current.locales[0]
    val todaysDayOfWeek = remember(currentLocale) { SimpleDateFormat("EEEE", currentLocale).format(Date()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(modifier = Modifier.testTag("habit_tracker_title")) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Habit Tracker")
                            Text(
                                text = todaysDayOfWeek,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.testTag("today_date_text")
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Box(modifier = Modifier.testTag("add_habit_fab")) {
                FloatingActionButton(
                    onClick = { showAddHabitDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                Box(modifier = Modifier.testTag("date_selector_container")) {
                    DateSelector(
                        selectedDate = selectedDate,
                        onDateSelected = { vm.setSelectedDate(it) },
                        modifier = Modifier.testTag("date_selector")
                    )
                }
            }

            item {
                Box(modifier = Modifier.testTag("daily_progress_diagram_container")) {
                    DailyProgressDiagram(viewModel = vm)
                }
            }

            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .testTag("empty_habits_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.testTag("empty_habits_text")) {
                            Text(
                                "No habits yet. Add your first habit!",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            } else {
                items(habits) { habit ->
                    Box(modifier = Modifier.testTag("habit_item_${habit.id}")) {
                        HabitItem(
                            habit = habit,
                            isCompleted = completions[habit.id] == true,
                            onToggleCompletion = { vm.toggleHabitCompletion(habit.id) },
                            onDelete = { vm.deleteHabit(habit.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    var showWeeklyProgress by remember { mutableStateOf(false) }

                    LaunchedEffect(showWeeklyProgress) {
                        if (showWeeklyProgress) {
                            vm.loadWeeklyProgress()
                        }
                    }

                    Box(modifier = Modifier.testTag("weekly_progress_card")) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showWeeklyProgress = !showWeeklyProgress }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.testTag("weekly_progress_title")) {
                                        Text(
                                            text = stringResource(id = R.string.weekly_progress_title),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Box(modifier = Modifier.testTag("weekly_progress_toggle_icon")) {
                                        Icon(
                                            imageVector = if (showWeeklyProgress) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (showWeeklyProgress) "Collapse" else "Expand"
                                        )
                                    }
                                }
                                if (showWeeklyProgress) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(modifier = Modifier.testTag("weekly_progress_view")) {
                                        WeeklyProgressView(viewModel = vm)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddHabitDialog) {
        Box(modifier = Modifier.testTag("add_habit_dialog")) {
            AddHabitDialog(
                onDismiss = { showAddHabitDialog = false },
                onAddHabit = { title, description ->
                    vm.addHabit(title, description)
                    showAddHabitDialog = false
                }
            )
        }
    }
}