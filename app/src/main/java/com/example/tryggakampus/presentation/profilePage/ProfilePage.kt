package com.example.tryggakampus.presentation.profilePage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.presentation.component.*
import com.example.tryggakampus.util.saveJsonToDownloads
import com.example.tryggakampus.R
import com.example.tryggakampus.util.HobbyList

@Composable
fun ProfilePage() {
    val vm: ProfileViewModel = viewModel()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
                    text = stringResource(R.string.account_information),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "${stringResource(R.string.username_label)}: ${vm.username}")
                Text(text = "${stringResource(R.string.email_label)}: ${vm.email}")
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Hobbies
            FormContainer {
                Text(
                    stringResource(R.string.my_hobbies),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val hobbiesScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .verticalScroll(hobbiesScrollState)
                ) {
                    Column {
                        vm.allHobbies.forEachIndexed { index, hobbyDisplayName ->
                            val hobbyKey = HobbyList.allHobbies[index].first
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = vm.hobbies.contains(hobbyKey),
                                    onCheckedChange = { vm.onHobbyToggle(hobbyKey) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.secondary,
                                        uncheckedColor = MaterialTheme.colorScheme.onPrimary,
                                        checkmarkColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = hobbyDisplayName)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                BlockButton(
                    onClick = { savingHobbies = true },
                    enabled = true
                ) {
                    Text(stringResource(R.string.save_hobbies))
                }
            }

            // Hobbies error
            Spacer(modifier = Modifier.height(16.dp))
            vm.hobbiesError?.let { ErrorBox(it.message) { vm.hobbiesError = null } }

            Spacer(modifier = Modifier.height(30.dp))

            // Change username
            FormContainer {
                Text(
                    stringResource(R.string.change_username),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedInput(
                    label = stringResource(R.string.password_required),
                    value = vm.usernameChangePassword,
                    onValueChange = { vm.onUsernameChangePasswordChange(it) },
                    isError = !vm.usernameChangePasswordIsValid,
                    isPassword = true,
                    isPasswordVisible = vm.isUsernameChangePasswordVisible,
                    onVisibilityChange = { vm.toggleUsernameChangePasswordVisibility() }
                )

                OutlinedInput(
                    label = stringResource(R.string.new_username),
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
                        Text(stringResource(R.string.update_username))
                    }
                }
            }

            // Username error
            Spacer(modifier = Modifier.height(16.dp))
            vm.usernameError?.let { ErrorBox(it.message) { vm.usernameError = null } }

            Spacer(modifier = Modifier.height(30.dp))

            // Change password
            FormContainer {
                Text(
                    stringResource(R.string.change_password),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedInput(
                    label = stringResource(R.string.current_password),
                    value = vm.currentPassword,
                    onValueChange = { vm.onCurrentPasswordChange(it) },
                    isError = !vm.currentPasswordIsValid,
                    isPassword = true,
                    isPasswordVisible = vm.isCurrentPasswordVisible,
                    onVisibilityChange = { vm.toggleCurrentPasswordVisibility() }
                )

                OutlinedInput(
                    label = stringResource(R.string.new_password),
                    value = vm.newPassword,
                    onValueChange = { vm.onNewPasswordChange(it) },
                    isError = !vm.newPasswordIsValid,
                    isPassword = true,
                    isPasswordVisible = vm.isNewPasswordVisible,
                    onVisibilityChange = { vm.toggleNewPasswordVisibility() }
                )

                OutlinedInput(
                    label = stringResource(R.string.repeat_new_password),
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
                        Text(stringResource(R.string.update_password))
                    }
                }
            }

            // Password error
            Spacer(modifier = Modifier.height(16.dp))
            vm.passwordError?.let { ErrorBox(it.message) { vm.passwordError = null } }

            Spacer(modifier = Modifier.height(30.dp))

            // Account and data
            FormContainer {
                Text(
                    stringResource(R.string.account_data),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                BlockButton(
                    onClick = { vm.showRequestDataDialog = true },
                    enabled = true
                ) {
                    Text(stringResource(R.string.request_my_data))
                }
                Spacer(modifier = Modifier.height(10.dp))

                BlockButton(
                    onClick = { vm.showDeleteAccountDialog = true },
                    enabled = true
                ) {
                    Text(stringResource(R.string.delete_account))
                }
            }

            // Delete account error
            Spacer(modifier = Modifier.height(16.dp))
            vm.deleteAccountError?.let { ErrorBox(it.message) { vm.deleteAccountError = null } }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Hobbies
    LaunchedEffect(Unit) {
        vm.loadAllHobbies(context)
    }

    LaunchedEffect(savingHobbies) {
        if (savingHobbies) {
            val success = vm.onSaveHobbies(context)
            if (success) snackbarHostState.showSnackbar(context.getString(R.string.hobbies_saved_success))
            savingHobbies = false
        }
    }

    // Username
    LaunchedEffect(updatingUsername) {
        if (updatingUsername) {
            val success = vm.onChangeUsername(context)
            if (success) snackbarHostState.showSnackbar(context.getString(R.string.username_changed_success))
            updatingUsername = false
        }
    }

    // Password
    LaunchedEffect(updatingPassword) {
        if (updatingPassword) {
            val success = vm.onChangePassword(context)
            if (success) snackbarHostState.showSnackbar(context.getString(R.string.password_changed_success))
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
        text = stringResource(R.string.profile_title),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun ConfirmDeleteAccountDialog(vm: ProfileViewModel) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { vm.showDeleteAccountDialog = false },
        title = { Text(stringResource(R.string.confirm_account_deletion)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.delete_account_warning),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedInput(
                    label = stringResource(R.string.enter_password),
                    value = vm.deletePassword,
                    onValueChange = { vm.deletePassword = it },
                    isError = vm.deletePassword.isEmpty()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { vm.onDeleteAccount(context) }) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.showDeleteAccountDialog = false }) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

@Composable
fun RequestDataDialog(vm: ProfileViewModel) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { vm.showRequestDataDialog = false },
        title = { Text(stringResource(R.string.personal_data_request)) },
        text = {
            Text(
                if (vm.jsonData == null)
                    stringResource(R.string.personal_data_request_message)
                else
                    stringResource(R.string.personal_data_ready_message)
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
                Text(
                    if (vm.jsonData == null)
                        stringResource(R.string.request)
                    else
                        stringResource(R.string.download),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.showRequestDataDialog = false }) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}
