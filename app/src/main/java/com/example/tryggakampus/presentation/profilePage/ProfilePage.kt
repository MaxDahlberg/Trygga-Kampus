package com.example.tryggakampus.presentation.profilePage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.presentation.component.*

@Composable
fun ProfilePage() {
    val vm: ProfileViewModel = viewModel<ProfileViewModel>()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        ProfileHeader()

        Spacer(modifier = Modifier.height(20.dp))

        // Account information
        FormContainer {
            Text(
                text = "Account Information",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Username: ${vm.username}")
            Text(text = "Email: ${vm.email}")
        }

        Spacer(modifier = Modifier.height(30.dp))


        // Change username
        FormContainer {
            Text("Change Username", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedInput(
                label = "Password (required)",
                value = vm.usernameChangePassword,
                onValueChange = { vm.usernameChangePassword = it },
                isError = !vm.usernameChangePasswordIsValid
            )

            OutlinedInput(
                label = "New Username",
                value = vm.newUsername,
                onValueChange = { vm.newUsername = it },
                isError = !vm.newUsernameIsValid
            )

            Spacer(modifier = Modifier.height(12.dp))
            BlockButton(
                onClick = { if (!vm.updatingUsername) vm.onChangeUsername() },
                enabled = vm.usernameChangePasswordIsValid && vm.newUsernameIsValid
            ) {
                if (vm.updatingUsername) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                } else {
                    Text("Update Username")
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))


        // Change password
        FormContainer {
            Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedInput(
                label = "Current Password",
                value = vm.currentPassword,
                onValueChange = { vm.currentPassword = it },
                isError = !vm.currentPasswordIsValid
            )

            OutlinedInput(
                label = "New Password",
                value = vm.newPassword,
                onValueChange = { vm.newPassword = it },
                isError = !vm.newPasswordIsValid
            )

            OutlinedInput(
                label = "Repeat New Password",
                value = vm.repeatNewPassword,
                onValueChange = { vm.repeatNewPassword = it },
                isError = vm.newPassword != vm.repeatNewPassword
            )

            Spacer(modifier = Modifier.height(12.dp))
            BlockButton(
                onClick = { if (!vm.updatingPassword) vm.onChangePassword() },
                enabled = vm.passwordChangeFormValid
            ) {
                if (vm.updatingPassword) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                } else {
                    Text("Update Password")
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))


        // Account and data
        FormContainer {
            Text("Account & Data", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))

            BlockButton(
                onClick = { vm.showRequestDataDialog = true },
                enabled = true
            ) {
                Text("Request My Data")
            }

            Spacer(modifier = Modifier.height(10.dp))

            BlockButton(
                onClick = { vm.showDeleteAccountDialog = true },
                enabled = true
            ) {
                Text("Delete My Data and Account")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        vm.error?.let {
            ErrorBox(it.message, onClick = { vm.clearError() })
        }
    }

    // Dialogs
    if (vm.showDeleteAccountDialog) ConfirmDeleteAccountDialog(vm)
    if (vm.showRequestDataDialog) RequestDataDialog(vm)
}

@Composable
fun ProfileHeader() {
    Text(
        text = "Profile",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun ConfirmDeleteAccountDialog(vm: ProfileViewModel) {
    AlertDialog(
        onDismissRequest = { vm.showDeleteAccountDialog = false },
        title = { Text("Confirm Account Deletion") },
        text = {
            Column {
                Text(
                    "Your personal data and account will be deleted in compliance with GDPR.\n\n This action is permanent and cannot be undone.",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedInput(
                    label = "Enter Password",
                    value = vm.deletePassword,
                    onValueChange = { vm.deletePassword = it },
                    isError = vm.deletePassword.isEmpty()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { vm.onDeleteAccount() }) {
                Text("Delete", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.showDeleteAccountDialog = false }) {
                Text("Cancel", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

@Composable
fun RequestDataDialog(vm: ProfileViewModel) {
    AlertDialog(
        onDismissRequest = { vm.showRequestDataDialog = false },
        title = { Text("Request Personal Data") },
        text = {
            Text(
                "Press Request to fetch your personal data from the server. When it's ready you'll be able to download it."
            )
        },
        confirmButton = {
            TextButton(onClick = {}) {  // todo: open new dialog for the download.
                Text("Request", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.showRequestDataDialog = false }) {
                Text("Cancel", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}