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

open class LoginViewModel : ViewModel() {
    open var email by mutableStateOf("")
        private set

    open var emailIsValid by mutableStateOf(true)
        private set

    open var passwordIsValid by mutableStateOf(true)
        private set

    open var password by mutableStateOf("")
        private set

    open var isPasswordVisible by mutableStateOf(false)
        private set

    open var passwordResetEmailSent by mutableStateOf(false)
        private set

    open var signingIn by mutableStateOf(false)
    open var error by mutableStateOf<AuthError?>(null)

    open fun clearError() {
        error = null
    }

    open fun onEmailChange(newEmail: String) {
        email = newEmail
        emailIsValid = android.util.Patterns
            .EMAIL_ADDRESS
            .matcher(email)
            .matches()
    }

    open fun onPasswordChange(newPassword: String) {
        password = newPassword
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\\\\-_{}.*]).{8,20}$".toRegex()
        passwordIsValid = password.matches(passwordPattern)
    }
    open fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
    }

    open fun onRequestLogin() {
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
    open fun onForgotPassword() {
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

    open fun dismissPasswordResetMessage() {
        passwordResetEmailSent = false
    }
    open fun onSignInWithGoogle(idToken: String) {
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

    open fun showSignInError(message: String) {
        error = AuthError(message)
    }
    open fun onSignInWithFacebook(token: String) {
        viewModelScope.launch {
            signingIn = true
            val authResponse = AuthRepositoryImpl.signInWithFacebook(token)
            error = when (authResponse) {
                AuthResponse.SignIn.ERROR -> AuthError("Facebook sign-in failed.")
                else -> null
            }
            signingIn = false
        }
    }
}

