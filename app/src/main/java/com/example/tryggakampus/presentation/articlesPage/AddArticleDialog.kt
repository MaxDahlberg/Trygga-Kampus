package com.example.tryggakampus.presentation.articlesPage

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.presentation.component.OutlinedInput
import com.example.tryggakampus.R
import androidx.compose.ui.res.stringResource

@Composable
fun AddArticleDialog(onDismiss: () -> Unit, viewModel: ArticlesPageViewModel) {
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var webpage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val errorMessage by viewModel.errorMessage.collectAsState()

    AlertDialog(
        onDismissRequest = {
            viewModel.clearErrorMessage()
            onDismiss()
        },
        title = { Text(stringResource(R.string.new_article)) },
        text = {
            Column {
                OutlinedInput(
                    label = stringResource(R.string.title),
                    value = title,
                    onValueChange = { title = it; showError = false },
                    isError = showError && title.isBlank()
                )

                OutlinedInput(
                    label = stringResource(R.string.summary),
                    value = summary,
                    onValueChange = { summary = it; showError = false },
                    isError = showError && summary.isBlank()
                )

                OutlinedInput(
                    label = stringResource(R.string.webpage_url),
                    value = webpage,
                    onValueChange = { webpage = it; showError = false },
                    isError = showError && webpage.isBlank()
                )

                if (showError) {
                    Text(
                        text = stringResource(R.string.all_fields_required),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && summary.isNotBlank() && webpage.isNotBlank()) {
                        viewModel.addArticle(title, summary, webpage)
                        if (viewModel.errorMessage.value == null) {
                            onDismiss()
                        }
                    } else {
                        showError = true
                    }
                },
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    viewModel.clearErrorMessage()
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

