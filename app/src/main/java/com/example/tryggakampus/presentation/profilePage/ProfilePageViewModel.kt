package com.example.tryggakampus.presentation.profilePage

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    // Hardcoded placeholders until db functionality is implemented.
    var username by mutableStateOf("UserName") // todo: load from database.
    var email by mutableStateOf("email@example.com") // todo: load from database.

    // Change username
    var newUsername by mutableStateOf("")
    var usernameChangePassword by mutableStateOf("")
    var updatingUsername by mutableStateOf(false)
    var newUsernameIsValid by mutableStateOf(true)
    var usernameChangePasswordIsValid by mutableStateOf(true)

    // Change password
    var currentPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var repeatNewPassword by mutableStateOf("")
    var updatingPassword by mutableStateOf(false)
    var currentPasswordIsValid by mutableStateOf(true)
    var newPasswordIsValid by mutableStateOf(true)

    val passwordChangeFormValid: Boolean
        get() = currentPasswordIsValid &&
                newPasswordIsValid &&
                newPassword == repeatNewPassword &&
                newPassword.length >= 8

    // Account deletion and user data
    var deletePassword by mutableStateOf("")
    var showDeleteAccountDialog by mutableStateOf(false)
    var showRequestDataDialog by mutableStateOf(false)

    // Error
    var error by mutableStateOf<AuthError?>(null)

    fun clearError() {
        error = null
    }

    // Change username
    fun onChangeUsername() {
        if (newUsername.isEmpty() || usernameChangePassword.isEmpty()) {
            error = AuthError("Please fill in all fields.")
            return
        }

        viewModelScope.launch {
            updatingUsername = true
            delay(1000)
            // todo: replace with real backend call.
            // val response = AuthRepositoryImpl.changeUsername(email, usernameChangePassword, newUsername)
            username = newUsername
            newUsername = ""
            usernameChangePassword = ""
            updatingUsername = false
        }
    }

    // Change password
    fun onChangePassword() {
        if (!passwordChangeFormValid) {
            error = AuthError("Passwords do not match or are invalid.")
            return
        }

        viewModelScope.launch {
            updatingPassword = true
            delay(1000)
            // todo: replace with real backend call.
            // val response = AuthRepositoryImpl.changePassword(email, currentPassword, newPassword)
            currentPassword = ""
            newPassword = ""
            repeatNewPassword = ""
            updatingPassword = false
        }
    }

    // Delete account
    fun onDeleteAccount() {
        if (deletePassword.isEmpty()) {
            error = AuthError("Password required to delete account.")
            return
        }

        viewModelScope.launch {
            delay(1000)
            // todo: replace with real backend call.
            // val response = AuthRepositoryImpl.deleteAccount(email, deletePassword)
            showDeleteAccountDialog = false
            error = AuthError("Account deletion not yet implemented.")
        }
    }

    // Request personal data.
    // fun onRequestData() {}
    // todo: add implementation.
}
