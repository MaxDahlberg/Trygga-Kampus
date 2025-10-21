package com.example.tryggakampus.presentation.profilePage

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.domain.repository.UserInformationRepository
import com.example.tryggakampus.domain.repository.UserInformationRepositoryImpl
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError
import com.example.tryggakampus.util.GdprUserDataHelper
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val userRepo = UserInformationRepositoryImpl
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser
    private val currentUserId = currentUser?.uid ?: ""

    // Account info
    var username by mutableStateOf("No username")
    var email by mutableStateOf(currentUser?.email ?: "No email")

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

    // Password visibility toggles
    var isUsernameChangePasswordVisible by mutableStateOf(false)
    var isCurrentPasswordVisible by mutableStateOf(false)
    var isNewPasswordVisible by mutableStateOf(false)
    var isRepeatPasswordVisible by mutableStateOf(false)

    // Account deletion and user data
    var deletePassword by mutableStateOf("")
    var showDeleteAccountDialog by mutableStateOf(false)
    var showRequestDataDialog by mutableStateOf(false)

    // Error
    var error by mutableStateOf<AuthError?>(null)
    fun clearError() { error = null }

    init {
        // Load username from Firestore if available
        if (currentUserId.isNotEmpty()) {
            viewModelScope.launch {
                val (result, userInfo) = userRepo.getUserInformation(currentUserId, com.google.firebase.firestore.Source.SERVER)
                if (result == UserInformationRepository.RepositoryResult.SUCCESS && userInfo != null) {
                    username = userInfo.username ?: username
                }
            }
        }
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
            error = null

            try {
                val email = currentUser?.email
                if (email.isNullOrBlank()) throw Exception("User email not available")

                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                currentUser?.reauthenticate(credential)?.await()

                currentUser?.updatePassword(newPassword)?.await()

                currentPassword = ""
                newPassword = ""
                repeatNewPassword = ""
            } catch (e: Exception) {
                error = AuthError("Password change failed: ${e.message}")
            }

            updatingPassword = false
        }
    }

    // Password validation regex
    private val passwordPattern =
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\\\\-_{}.*]).{8,20}$".toRegex()

    fun onUsernameChangePasswordChange(password: String) {
        usernameChangePassword = password
        usernameChangePasswordIsValid = password.matches(passwordPattern)
    }

    fun onCurrentPasswordChange(password: String) {
        currentPassword = password
        currentPasswordIsValid = password.matches(passwordPattern)
    }

    fun onNewPasswordChange(password: String) {
        newPassword = password
        newPasswordIsValid = password.matches(passwordPattern)
    }

    // Toggle functions
    fun toggleUsernameChangePasswordVisibility() {
        isUsernameChangePasswordVisible = !isUsernameChangePasswordVisible
    }

    fun toggleCurrentPasswordVisibility() {
        isCurrentPasswordVisible = !isCurrentPasswordVisible
    }

    fun toggleNewPasswordVisibility() {
        isNewPasswordVisible = !isNewPasswordVisible
    }

    fun toggleRepeatPasswordVisibility() {
        isRepeatPasswordVisible = !isRepeatPasswordVisible
    }

    // Delete account
    fun onDeleteAccount() {
        if (deletePassword.isEmpty()) {
            error = AuthError("Password required to delete account.")
            return
        }

        viewModelScope.launch {
            try {
                val email = currentUser?.email ?: throw Exception("User email not available")
                val credential = EmailAuthProvider.getCredential(email, deletePassword)
                currentUser?.reauthenticate(credential)?.await()

                currentUser?.uid?.let { GdprUserDataHelper().deleteUserData(it) }
                currentUser?.delete()?.await()
                showDeleteAccountDialog = false
            } catch (e: Exception) {
                error = AuthError("Account deletion failed: ${e.message}")
            }
        }
    }

    // Request personal data.
    // fun onRequestData() {}
    // todo: add implementation.
}
