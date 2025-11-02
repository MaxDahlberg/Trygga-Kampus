@file:Suppress("unused", "UNUSED_PARAMETER")

package com.example.tryggakampus.presentation.authentication.registerPage

import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestRegisterViewModel : RegisterViewModel() {

    override var email: String by mutableStateOf("")
    override var password: String by mutableStateOf("")
    override var emailIsValid: Boolean by mutableStateOf(true)
    override var passwordIsValid: Boolean by mutableStateOf(true)
    override var signingUp: Boolean by mutableStateOf(false)
    override var error: AuthError? by mutableStateOf(null)

    private var fakeSignUpSuccess = true
    private var fakeEmailTaken = false

    fun setFakeSignUpSuccess(success: Boolean) { fakeSignUpSuccess = success }
    fun setFakeEmailTaken(taken: Boolean) { fakeEmailTaken = taken }
    fun setEmailForTest(value: String) { onEmailChange(value) }
    fun setPasswordForTest(value: String) { onPasswordChange(value) }
    fun setSigningUpForTest(isSigning: Boolean) { signingUp = isSigning }
    fun setErrorForTest(message: String) { error = AuthError(message) }

    override fun onRequestSignUp() {
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
            delay(500) // simulate network
            error = when {
                fakeEmailTaken -> AuthError("This email is already taken")
                fakeSignUpSuccess -> null
                else -> AuthError("Sign up failed. Please check your details.")
            }
            signingUp = false
        }
    }

    override fun onPasswordChange(newPassword: String) {
        password = newPassword
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!\\\\-_{}.*]).{8,20}$".toRegex()
        passwordIsValid = newPassword.matches(passwordPattern)
    }

    override fun onEmailChange(newEmail: String) {
        email = newEmail
        emailIsValid = android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()
    }

    override fun clearError() { error = null }
}
