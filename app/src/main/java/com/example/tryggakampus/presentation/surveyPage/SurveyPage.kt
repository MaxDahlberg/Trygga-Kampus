package com.example.tryggakampus.presentation.surveyPage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val questions = remember { SurveyQuestions.getQuestions(context) }
    var answers = remember { mutableStateListOf(*Array(questions.size) { "" }) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    val viewModel: SurveyViewModel = viewModel(factory = SurveyViewModelFactory())
    val isFormComplete by remember { derivedStateOf { answers.all { it.isNotBlank() } } }

    val snackbarHostState = remember { SnackbarHostState() }
    var submitting by remember { mutableStateOf(false) }

    // Pre-fetch string resources to use in suspend functions
    val successMessage = stringResource(R.string.survey_submit_success)
    val failureMessage = stringResource(R.string.survey_submit_failed)

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor =  MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            item {
                Text(
                    text = stringResource(R.string.survey_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

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

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isFormComplete && !submitting) {
                            submitting = true
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
                    enabled = isFormComplete && !submitting
                ) {
                    Text(stringResource(R.string.submit_answers), fontSize = 18.sp)
                }
            }
        }
    }

    LaunchedEffect(submitting) {
        if (submitting) {
            val result = runCatching {
                viewModel.submitSurvey(questions, answers)
            }

            // Clear the form regardless of success or failure
            answers.clear()
            answers.addAll(Array(questions.size) { "" })

            if (result.isSuccess) {
                snackbarHostState.showSnackbar(successMessage)
            } else {
                snackbarHostState.showSnackbar(failureMessage)
            }

            submitting = false
        }
    }
}
