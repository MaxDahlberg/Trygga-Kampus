package com.example.tryggakampus.presentation.profilePage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.presentation.component.*
import com.example.tryggakampus.util.saveJsonToDownloads

@Composable
fun ProfilePage() {
    val vm: ProfileViewModel = viewModel()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    var savingHobbies by remember { mutableStateOf(false) }
    var updatingUsername by remember { mutableStateOf(false) }
    var updatingPassword by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(paddingValues),
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

            // Hobbies
            FormContainer {
                Text("My Hobbies", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))

                val hobbiesScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .verticalScroll(hobbiesScrollState)
                ) {
                    Column {
                        vm.allHobbies.forEach { hobby ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = vm.hobbies.contains(hobby),
                                    onCheckedChange = { vm.onHobbyToggle(hobby) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.secondary,
                                        uncheckedColor = MaterialTheme.colorScheme.onPrimary,
                                        checkmarkColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = hobby)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                BlockButton(
                    onClick = { savingHobbies = true }, // trigger snackbar
                    enabled = true
                ) {
                    Text("Save Hobbies")
                }
            }

            // Hobbies error
            Spacer(modifier = Modifier.height(16.dp))
            vm.hobbiesError?.let { ErrorBox(it.message) { vm.hobbiesError = null } }

            Spacer(modifier = Modifier.height(30.dp))

            // Change username
            FormContainer {
                Text("Change Username", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedInput(
                    label = "Password (required)",
                    value = vm.usernameChangePassword,
                    onValueChange = { vm.onUsernameChangePasswordChange(it) },
                    isError = !vm.usernameChangePasswordIsValid,
                    isPassword = true,
                    isPasswordVisible = vm.isUsernameChangePasswordVisible,
                    onVisibilityChange = { vm.toggleUsernameChangePasswordVisibility() }
                )

                OutlinedInput(
                    label = "New Username",
                    value = vm.newUsername,
                    onValueChange = { vm.newUsername = it },
                    isError = !vm.newUsernameIsValid
                )

                Spacer(modifier = Modifier.height(12.dp))
                BlockButton(
                    onClick = { updatingUsername = true },
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

            // Username error
            Spacer(modifier = Modifier.height(16.dp))
            vm.usernameError?.let { ErrorBox(it.message) { vm.usernameError = null } }

            Spacer(modifier = Modifier.height(30.dp))

            // Change password
            FormContainer {
                Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedInput(
                    label = "Current Password",
                    value = vm.currentPassword,
                    onValueChange = { vm.onCurrentPasswordChange(it) },
                    isError = !vm.currentPasswordIsValid,
                    isPassword = true,
                    isPasswordVisible = vm.isCurrentPasswordVisible,
                    onVisibilityChange = { vm.toggleCurrentPasswordVisibility() }
                )

                OutlinedInput(
                    label = "New Password",
                    value = vm.newPassword,
                    onValueChange = { vm.onNewPasswordChange(it) },
                    isError = !vm.newPasswordIsValid,
                    isPassword = true,
                    isPasswordVisible = vm.isNewPasswordVisible,
                    onVisibilityChange = { vm.toggleNewPasswordVisibility() }
                )

                OutlinedInput(
                    label = "Repeat New Password",
                    value = vm.repeatNewPassword,
                    onValueChange = { vm.repeatNewPassword = it },
                    isError = vm.newPassword != vm.repeatNewPassword,
                    isPassword = true,
                    isPasswordVisible = vm.isRepeatPasswordVisible,
                    showPasswordRules = true,
                    onVisibilityChange = { vm.toggleRepeatPasswordVisibility() }
                )

                Spacer(modifier = Modifier.height(12.dp))
                BlockButton(
                    onClick = { updatingPassword = true },
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

            // Password error
            Spacer(modifier = Modifier.height(16.dp))
            vm.passwordError?.let { ErrorBox(it.message) { vm.passwordError = null } }

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

            // Delete account error
            Spacer(modifier = Modifier.height(16.dp))
            vm.deleteAccountError?.let { ErrorBox(it.message) { vm.deleteAccountError = null } }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Hobbies
    LaunchedEffect(savingHobbies) {
        if (savingHobbies) {
            val success = vm.onSaveHobbies()
            if (success) snackbarHostState.showSnackbar("Hobbies saved successfully!")
            savingHobbies = false
        }
    }

    // Username
    LaunchedEffect(updatingUsername) {
        if (updatingUsername) {
            val success = vm.onChangeUsername()
            if (success) snackbarHostState.showSnackbar("Username changed successfully!")
            updatingUsername = false
        }
    }

    // Password
    LaunchedEffect(updatingPassword) {
        if (updatingPassword) {
            val success = vm.onChangePassword()
            if (success) snackbarHostState.showSnackbar("Password changed successfully!")
            updatingPassword = false
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
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { vm.showRequestDataDialog = false },
        title = { Text("Personal Data Request") },
        text = {
            Text(
                if (vm.jsonData == null)
                    "Press Request to fetch your personal data from the server. When it's ready, you'll be able to download it."
                else
                    "Your personal data is ready. Press Download to save it to your device."
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (vm.jsonData == null) {
                    vm.onRequestData() // fetch JSON
                } else {
                    vm.jsonData?.let { data ->
                        saveJsonToDownloads(context, data, "personal_data.json")
                        vm.showRequestDataDialog = false
                        vm.resetJsonData()
                    }
                }
            }) {
                Text(if (vm.jsonData == null) "Request" else "Download", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.showRequestDataDialog = false }) {
                Text("Cancel", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}
