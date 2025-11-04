package com.example.tryggakampus.presentation.landingPage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.R
import com.example.tryggakampus.presentation.component.OutlinedInput

@Composable
fun UsernameDialog(viewModel: LandingPageViewModel) {
    val context = LocalContext.current

    var usernameInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val usernameError by viewModel.usernameError.collectAsState()
    val updating by viewModel.updatingUsername.collectAsState()

    AlertDialog(
        onDismissRequest = { /* Mandatory, cannot dismiss */ },
        title = { Text(stringResource(R.string.choose_username)) },
        text = {
            Column {
                OutlinedInput(
                    label = stringResource(R.string.username_label),
                    value = usernameInput,
                    onValueChange = { usernameInput = it; showError = false },
                    isError = showError || usernameError != null
                )

                if (showError) {
                    Text(
                        text = stringResource(R.string.username_empty),
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
                        viewModel.updateUsername(usernameInput, context) { success ->
                            // The dialog will automatically close in LandingPage once ViewModel updates username
                        }
                    } else {
                        showError = true
                    }
                },
                enabled = !updating
            ) {
                Text(text = if (updating) stringResource(R.string.submitting) else stringResource(R.string.submit))
            }
        },
        // No dismiss button since user must select a username
    )
}
