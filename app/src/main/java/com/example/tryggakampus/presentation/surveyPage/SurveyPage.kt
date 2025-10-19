// app/src/main/java/com/example/tryggakampus/presentation/surveyPage/SurveyPage.kt
package com.example.tryggakampus.presentation.surveyPage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.R
import com.example.tryggakampus.data.SurveyQuestions
import com.example.tryggakampus.data.repository.SurveyViewModelFactory

@Composable
fun SurveyPage(title: String) {
    val context = LocalContext.current
    val questions = remember { SurveyQuestions.getQuestions(context) } // Pass context here
    var answers = remember { mutableStateListOf(*Array(questions.size) { "" }) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    val viewModel: SurveyViewModel = viewModel(factory = SurveyViewModelFactory())
    val isFormComplete by remember {
        derivedStateOf { answers.all { it.isNotBlank() } }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        item {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // questions and inputfield area
        itemsIndexed(questions) { index, question ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.question_prefix, index + 1) + " " + question,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = answers[index],
                        onValueChange = { answers[index] = it },
                        label = { Text(stringResource(R.string.your_answer), color = MaterialTheme.colorScheme.onBackground) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedIndicatorColor = MaterialTheme.colorScheme.secondary,

                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedIndicatorColor = Color.Transparent,

                            cursorColor = MaterialTheme.colorScheme.secondary,

                            selectionColors = TextSelectionColors(
                                handleColor = MaterialTheme.colorScheme.secondary,
                                backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            )
                        )
                    )
                }
            }
        }

        // button area after the questions
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isFormComplete) {
                        viewModel.submitSurvey(questions, answers)
                        showCompletionDialog = true

                        // Reset the answers list
                        answers.clear()
                        answers.addAll(Array(questions.size) { "" })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                enabled = isFormComplete
            ) {
                Text(stringResource(R.string.submit_answers), fontSize = 18.sp)
            }
        }
    }

    // Completion Dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = { Text(stringResource(R.string.survey_complete_title)) },
            text = { Text(stringResource(R.string.survey_complete_message)) },
            confirmButton = {
                Button(onClick = { showCompletionDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}