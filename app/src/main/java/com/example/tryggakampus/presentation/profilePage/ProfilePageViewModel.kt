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
}
