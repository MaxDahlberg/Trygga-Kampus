package com.example.tryggakampus.presentation.profilePage

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.domain.model.UserInfoModel
import com.example.tryggakampus.domain.repository.UserInformationRepository
import com.example.tryggakampus.domain.repository.UserInformationRepositoryImpl
import com.example.tryggakampus.util.GdprUserDataHelper
import com.example.tryggakampus.util.HobbyList
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError

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
    var jsonData by mutableStateOf<String?>(null)

    // Errors
    var usernameError by mutableStateOf<AuthError?>(null)
    var passwordError by mutableStateOf<AuthError?>(null)
    var hobbiesError by mutableStateOf<AuthError?>(null)
    var deleteAccountError by mutableStateOf<AuthError?>(null)

    // Hobbies
    var hobbies by mutableStateOf<List<String>>(emptyList())
    var allHobbies by mutableStateOf(HobbyList.allHobbies)

    init {
        // Load username and hobbies from Firestore
        if (currentUserId.isNotEmpty()) {
            viewModelScope.launch {
                val (result, userInfo) = userRepo.getUserInformation(currentUserId, com.google.firebase.firestore.Source.SERVER)
                if (result == UserInformationRepository.RepositoryResult.SUCCESS && userInfo != null) {
                    username = userInfo.username ?: username
                    hobbies = userInfo.hobbies
                }
            }
        }
    }

    // Hobbies
    fun onHobbyToggle(hobby: String) {
        hobbies = if (hobbies.contains(hobby)) {
            hobbies - hobby
        } else {
            hobbies + hobby
        }
    }

    fun onSaveHobbies() {
        viewModelScope.launch {
            val result = userRepo.addOrUpdateUserInformation(
                userInfo = UserInfoModel(userId = currentUserId),
                updateFields = mapOf("hobbies" to hobbies)
            )
            if (result != UserInformationRepository.RepositoryResult.SUCCESS) {
                hobbiesError = AuthError("Failed to update hobbies")
            } else {
                hobbiesError = null
            }
        }
    }

    // Change username
    fun onChangeUsername() {
        if (newUsername.isBlank() || usernameChangePassword.isBlank()) {
            usernameError = AuthError("Please fill in all fields.")
            return
        }

        val email = currentUser?.email
        if (email.isNullOrBlank()) {
            usernameError = AuthError("User email not available.")
            return
        }

        viewModelScope.launch {
            updatingUsername = true
            usernameError = null

            try {
                val credential = EmailAuthProvider.getCredential(email, usernameChangePassword)
                currentUser?.reauthenticate(credential)?.await()

                val available = UserInformationRepositoryImpl.isUsernameAvailable(newUsername)
                if (!available) {
                    usernameError = AuthError("This username is already taken.")
                    updatingUsername = false
                    return@launch
                }

                val result = UserInformationRepositoryImpl.addOrUpdateUserInformation(
                    userInfo = UserInfoModel(userId = currentUserId),
                    updateFields = mapOf("username" to newUsername)
                )

                if (result == UserInformationRepository.RepositoryResult.SUCCESS) {
                    username = newUsername
                    newUsername = ""
                    usernameChangePassword = ""
                    usernameError = null
                } else {
                    usernameError = AuthError("Failed to update username: $result")
                }

            } catch (e: Exception) {
                usernameError = AuthError("Failed to update username: ${e.message}")
            } finally {
                updatingUsername = false
            }
        }
    }

    // Change password
    fun onChangePassword() {
        if (!passwordChangeFormValid) {
            passwordError = AuthError("Passwords do not match or are invalid.")
            return
        }

        viewModelScope.launch {
            updatingPassword = true
            passwordError = null

            try {
                val email = currentUser?.email
                if (email.isNullOrBlank()) throw Exception("User email not available")

                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                currentUser?.reauthenticate(credential)?.await()

                currentUser?.updatePassword(newPassword)?.await()

                currentPassword = ""
                newPassword = ""
                repeatNewPassword = ""
                passwordError = null
            } catch (e: Exception) {
                passwordError = AuthError("Password change failed: ${e.message}")
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
            deleteAccountError = AuthError("Password required to delete account.")
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
                deleteAccountError = null
            } catch (e: Exception) {
                deleteAccountError = AuthError("Account deletion failed: ${e.message}")
            }
        }
    }

    // Request personal data
    fun onRequestData() {
        viewModelScope.launch {
            if (jsonData == null) {
                try {
                    jsonData = GdprUserDataHelper().fetchUserData(currentUserId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun resetJsonData() {
        jsonData = null
    }
}
