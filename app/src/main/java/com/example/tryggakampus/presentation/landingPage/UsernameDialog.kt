package com.example.tryggakampus.presentation.landingPage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.presentation.component.OutlinedInput

@Composable
fun UsernameDialog(viewModel: LandingPageViewModel) {
    var usernameInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val usernameError by viewModel.usernameError.collectAsState()
    val updating by viewModel.updatingUsername.collectAsState()

    AlertDialog(
        onDismissRequest = { /* Mandatory, cannot dismiss */ },
        title = { Text(text = "Choose a username") },
        text = {
            Column {
                OutlinedInput(
                    label = "Username",
                    value = usernameInput,
                    onValueChange = { usernameInput = it; showError = false },
                    isError = showError || usernameError != null
                )

                if (showError) {
                    Text(
                        text = "Username cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (usernameError != null) {
                    Text(
                        text = usernameError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (usernameInput.isNotBlank()) {
                        viewModel.updateUsername(usernameInput) { success ->
                            // The dialog will automatically close in LandingPage once ViewModel updates username
                        }
                    } else {
                        showError = true
                    }
                },
                enabled = !updating
            ) {
                Text(text = if (updating) "Submitting..." else "Submit")
            }
        },
        // No dismiss button since user must select a username
    )
}
