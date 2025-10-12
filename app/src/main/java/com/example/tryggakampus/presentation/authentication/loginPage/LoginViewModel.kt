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

    var isPasswordVisible by mutableStateOf(false)
        private set

    var passwordResetEmailSent by mutableStateOf(false)
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
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\\\\-_{}.*]).{8,20}$".toRegex()
        passwordIsValid = password.matches(passwordPattern)
    }
    fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
    }

    fun onRequestLogin() {
        if (email.isEmpty() || password.isEmpty()) {
            error = AuthError("Wrong username or password")
            return
        }

        viewModelScope.launch {
            signingIn = true

            val authResponse = AuthRepositoryImpl.authenticateUser(email, password)

            error = when (authResponse) {
                AuthResponse.SignIn.FAILURE -> {
                    AuthError("Wrong username or password")
                }

                AuthResponse.SignIn.ERROR -> {
                    AuthError("Something went wrong, please try again later.")
                }

                else -> null
            }

            signingIn = false
        }
    }
    fun onForgotPassword() {
        if (!emailIsValid) {
            error = AuthError("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            signingIn = true
            val resetResponse = AuthRepositoryImpl.sendPasswordResetEmail(email)

            when (resetResponse) {
                AuthResponse.PasswordReset.SUCCESS -> {
                    error = null
                    passwordResetEmailSent = true
                }
                AuthResponse.PasswordReset.FAILURE -> {
                    error = AuthError("Failed to send reset email. Please ensure the email is correct.")
                    passwordResetEmailSent = false
                }
            }
            signingIn = false
        }
    }

    fun dismissPasswordResetMessage() {
        passwordResetEmailSent = false
    }
    fun onSignInWithGoogle(idToken: String) {
        viewModelScope.launch {
            signingIn = true
            val authResponse = AuthRepositoryImpl.signInWithGoogle(idToken)
            error = when (authResponse) {
                AuthResponse.SignIn.ERROR -> AuthError("Google sign-in failed.")
                else -> null
            }
            signingIn = false
        }
    }

    fun showSignInError(message: String) {
        error = AuthError(message)
    }
}

