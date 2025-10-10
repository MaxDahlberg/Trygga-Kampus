package com.example.tryggakampus.presentation.profilePage

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError

class ProfileViewModel : ViewModel() {

    // Hardcoded placeholders until db functionality is implemented.
    var username by mutableStateOf("UserName") // todo: load from database.
    var email by mutableStateOf("email@example.com") // todo: load from database.

    // Error
    var error by mutableStateOf<AuthError?>(null)

    fun clearError() {
        error = null
    }
}
