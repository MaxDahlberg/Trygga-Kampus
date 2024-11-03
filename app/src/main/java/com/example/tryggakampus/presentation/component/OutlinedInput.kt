package com.example.tryggakampus.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun OutlinedInput(
    label: String,
    value: String = "",
    onValueChange: (v: String) -> Unit,
    isError: Boolean = false
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        value = value,
        onValueChange = onValueChange,
        isError = isError,
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