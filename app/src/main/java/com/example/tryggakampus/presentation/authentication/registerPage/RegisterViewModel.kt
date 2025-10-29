package com.example.tryggakampus.presentation.authentication.registerPage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.domain.repository.AuthRepositoryImpl
import com.example.tryggakampus.domain.repository.AuthResponse
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class RegisterViewModel : ViewModel() {
    var email by mutableStateOf(""); private set
    var emailIsValid by mutableStateOf(true); private set
    var password by mutableStateOf(""); private set
    var passwordIsValid by mutableStateOf(true); private set
    var isPasswordVisible by mutableStateOf(false); private set
    var signingUp by mutableStateOf(false); private set
    var error by mutableStateOf<AuthError?>(null); private set
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
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\\-_{}.*]).{8,20}$".toRegex()
        passwordIsValid = password.matches(passwordPattern)
    }
    fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
    }
    fun onRequestSignUp() {

        if (email.isEmpty() || password.isEmpty()) {
            error = AuthError("Email and password cannot be empty")
            return
        }
        if (!emailIsValid) {
            error = AuthError("Please enter a valid email address")
            return
        }
        if (!passwordIsValid) {
            error = AuthError("Password does not meet the requirements. It must be 8-20 characters and include an uppercase letter, a number, and a special character.")
            return
        }
        viewModelScope.launch {
            signingUp = true
            delay(2000)
            val authResponse = AuthRepositoryImpl.registerUser(email, password)

            when (authResponse) {
                AuthResponse.SignUp.FAILURE -> {
                    error = AuthError("Sign up failed. Please check your details.")
                }
                AuthResponse.SignUp.EMAIL_TAKEN -> {
                    error = AuthError("This email is already taken")
                }
                AuthResponse.SignUp.ERROR -> {
                    error = AuthError("Something went wrong, please try again later.")
                }
                else -> {
                    error = null
                }
            }
            signingUp = false
        }
    }
}
