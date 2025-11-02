@file:Suppress("unused", "UNUSED_PARAMETER")

package com.example.tryggakampus.presentation.authentication.loginPage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestLoginViewModel : LoginViewModel() {

    override var email by mutableStateOf("")
    override var emailIsValid by mutableStateOf(true)
    override var passwordIsValid by mutableStateOf(true)
    override var password by mutableStateOf("")
    override var isPasswordVisible by mutableStateOf(false)
    override var passwordResetEmailSent by mutableStateOf(false)
    override var signingIn by mutableStateOf(false)
    override var error by mutableStateOf<AuthError?>(null)

    override fun onEmailChange(newEmail: String) {
        email = newEmail
        emailIsValid = android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()
    }

    override fun onPasswordChange(newPassword: String) {
        password = newPassword
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\\\\-_{}.*]).{8,20}$".toRegex()
        passwordIsValid = newPassword.matches(passwordPattern)
    }

    override fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
    }

    override fun onRequestLogin() {
        if (email.isEmpty() || password.isEmpty()) {
            error = AuthError("Wrong username or password")
            return
        }

        viewModelScope.launch {
            signingIn = true
            delay(500)
            error = null
            signingIn = false
        }
    }

    override fun onForgotPassword() {
        if (!emailIsValid) {
            error = AuthError("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            signingIn = true
            delay(500)
            error = null
            passwordResetEmailSent = true
            signingIn = false
        }
    }

    override fun dismissPasswordResetMessage() {
        passwordResetEmailSent = false
    }

    override fun onSignInWithGoogle(idToken: String) {
        viewModelScope.launch {
            signingIn = true
            delay(500)
            error = null
            signingIn = false
        }
    }

    override fun onSignInWithFacebook(token: String) {
        viewModelScope.launch {
            signingIn = true
            delay(500)
            error = null
            signingIn = false
        }
    }

    override fun showSignInError(message: String) {
        error = AuthError(message)
    }

    override fun clearError() {
        error = null
    }

    fun setErrorForTest(message: String) {
        error = AuthError(message)
    }

    fun setPasswordResetSentForTest(sent: Boolean) {
        passwordResetEmailSent = sent
    }

    fun setSigningInForTest(isSigning: Boolean) {
        signingIn = isSigning
    }
}
