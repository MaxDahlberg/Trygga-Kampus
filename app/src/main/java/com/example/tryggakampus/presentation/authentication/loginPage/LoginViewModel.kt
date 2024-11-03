package com.example.tryggakampus.presentation.authentication.loginPage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.domain.repository.AuthRepositoryImpl
import com.example.tryggakampus.domain.repository.AuthResponse
import kotlinx.coroutines.launch

data class AuthError(val message: String)

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set

    var emailIsValid by mutableStateOf(true)
        private set

    var passwordIsValid by mutableStateOf(true)
        private set

    var password by mutableStateOf("")
        private set

    var signingIn by mutableStateOf(false)
    var error by mutableStateOf<AuthError?>(null)

    fun clearError() {
        error = null
    }

    fun onEmailChange(newEmail: String) {
        email = newEmail
        emailIsValid = android.util.Patterns
            .EMAIL_ADDRESS
            .matcher(email)
            .matches()
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        passwordIsValid = password.length >= 8
    }

    fun onRequestLogin() {
        if (email.isEmpty() || password.isEmpty()) {
            error = AuthError("Wrong username or password")
            return
        }

        viewModelScope.launch {
            signingIn = true

            val authResponse = AuthRepositoryImpl.authenticateUser(email, password)

            when (authResponse) {
                AuthResponse.SignIn.FAILURE -> {
                    error = AuthError("Wrong username or password")
                }

                AuthResponse.SignIn.ERROR -> {
                    error = AuthError("Something went wrong, please try again later.")
                }

                else -> error = null
            }

            signingIn = false
        }
    }
}
