package com.example.tryggakampus.presentation.habitTracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.tryggakampus.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerPage(
    viewModel: HabitTrackerViewModel,
    onNavigateBack: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val completions by viewModel.completions.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }

    val currentLocale = LocalConfiguration.current.locales[0]
    val todaysDayOfWeek = remember(currentLocale) { SimpleDateFormat("EEEE", currentLocale).format(Date()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Habit Tracker")
                        Text(
                            text = todaysDayOfWeek,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHabitDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                DateSelector(
                    selectedDate = selectedDate,
                    onDateSelected = viewModel::setSelectedDate
                )
            }

            item {
                DailyProgressDiagram(viewModel = viewModel)
            }

            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No habits yet. Add your first habit!",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                items(habits) { habit ->
                    HabitItem(
                        habit = habit,
                        isCompleted = completions[habit.id] == true,
                        onToggleCompletion = { viewModel.toggleHabitCompletion(habit.id) },
                        onDelete = { viewModel.deleteHabit(habit.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    var showWeeklyProgress by remember { mutableStateOf(false) }

                    LaunchedEffect(showWeeklyProgress) {
                        if (showWeeklyProgress) {
                            viewModel.loadWeeklyProgress()
                        }
                    }

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
                                Text(
                                    text = stringResource(id = R.string.weekly_progress_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    imageVector = if (showWeeklyProgress) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showWeeklyProgress) "Collapse" else "Expand"
                                )
                            }
                            if (showWeeklyProgress) {
                                Spacer(modifier = Modifier.height(16.dp))
                                WeeklyProgressView(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onAddHabit = { title, description ->
                viewModel.addHabit(title, description)
                showAddHabitDialog = false
            }
        )
    }
}