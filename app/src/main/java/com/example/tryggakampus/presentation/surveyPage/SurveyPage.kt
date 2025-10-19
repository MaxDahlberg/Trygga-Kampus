package com.example.tryggakampus.presentation.surveyPage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.data.models.EvaluationType
import com.example.tryggakampus.data.models.Question
import com.example.tryggakampus.data.models.QuestionType
import com.example.tryggakampus.di.AppContainer
import com.example.tryggakampus.di.ViewModelFactory
import kotlinx.coroutines.launch


val morningCheckInQuestions = listOf(
    Question(id = "q_mood", text = "How are you feeling this morning?", type = QuestionType.SLIDER_1_5),
    Question(id = "q_sleep_quality", text = "How would you rate your sleep quality?", type = QuestionType.SLIDER_1_5),
    Question(id = "q_self_esteem", text = "On a scale of 1-10, how high is your self-esteem right now?", type = QuestionType.SLIDER_1_10)
)

@Composable
fun SurveyPage(title: String) {
    val viewModel: SurveyPageViewModel = viewModel(
        factory = ViewModelFactory(AppContainer.provideEvaluationRepository())
    )

    val questions = morningCheckInQuestions
    val answers = remember { mutableStateMapOf<String, Any>() }

    val isFormComplete by remember {
        derivedStateOf { answers.size == questions.size }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            itemsIndexed(questions) { _, question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = question.text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        when (question.type) {
                            QuestionType.SLIDER_1_5, QuestionType.SLIDER_1_10 -> {
                                val maxRange = if (question.type == QuestionType.SLIDER_1_10) 10f else 5f
                                val sliderValue = (answers[question.id] as? Float) ?: 1f

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Slider(
                                        value = sliderValue,
                                        onValueChange = { answers[question.id] = it },
                                        valueRange = 1f..maxRange,
                                        steps = (maxRange - 2).toInt(),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = sliderValue.toInt().toString(),
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                            QuestionType.TEXT_INPUT -> {
                                OutlinedTextField(
                                    value = (answers[question.id] as? String) ?: "",
                                    onValueChange = { answers[question.id] = it },
                                    label = { Text("Your Answer") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            // We can add more types like MULTIPLE_CHOICE here later
                            else -> {}
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            // PREPARE and SUBMIT the data in the correct format
                            val selfEsteemAnswer = (answers["q_self_esteem"] as? Float)?.toInt()

                            viewModel.submitEvaluation(
                                type = EvaluationType.MORNING_CHECK_IN,
                                answers = answers.toMap(), // Convert the state map to a regular map
                                selfEsteemScore = selfEsteemAnswer
                            )

                            // Show success message and clear the form
                            snackbarHostState.showSnackbar("Evaluation submitted successfully!")
                            answers.clear()
                            isSubmitting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = isFormComplete && !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Submit Evaluation", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
