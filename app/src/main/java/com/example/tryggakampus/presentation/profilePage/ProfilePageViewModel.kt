package com.example.tryggakampus.presentation.profilePage

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.domain.repository.UserInformationRepository
import com.example.tryggakampus.domain.repository.UserInformationRepositoryImpl
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError
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
