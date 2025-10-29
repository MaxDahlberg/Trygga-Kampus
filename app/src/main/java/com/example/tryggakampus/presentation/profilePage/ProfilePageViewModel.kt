package com.example.tryggakampus.presentation.profilePage

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.R
import com.example.tryggakampus.domain.model.UserInfoModel
import com.example.tryggakampus.domain.repository.UserInformationRepository
import com.example.tryggakampus.domain.repository.UserInformationRepositoryImpl
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError
import com.example.tryggakampus.util.GdprUserDataHelper
import com.example.tryggakampus.util.HobbyList
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
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
        if (currentUserId.isNotEmpty()) {
            viewModelScope.launch {
                val (result, userInfo) = userRepo.getUserInformation(
                    currentUserId,
                    com.google.firebase.firestore.Source.SERVER
                )
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

    suspend fun onSaveHobbies(context: Context): Boolean {
        return try {
            val result = userRepo.addOrUpdateUserInformation(
                userInfo = UserInfoModel(userId = currentUserId),
                updateFields = mapOf("hobbies" to hobbies)
            )
            if (result == UserInformationRepository.RepositoryResult.SUCCESS) {
                hobbiesError = null
                true
            } else {
                hobbiesError = AuthError(context.getString(R.string.error_update_hobbies))
                false
            }
        } catch (e: Exception) {
            hobbiesError = AuthError(
                context.getString(R.string.error_update_hobbies_detail, e.message ?: "")
            )
            false
        }
    }

    // Change username
    suspend fun onChangeUsername(context: Context): Boolean {
        if (newUsername.isBlank() || usernameChangePassword.isBlank()) {
            usernameError = AuthError(context.getString(R.string.error_fill_all_fields))
            return false
        }

        val email = currentUser?.email ?: run {
            usernameError = AuthError(context.getString(R.string.error_user_email_unavailable))
            return false
        }

        return try {
            val credential = EmailAuthProvider.getCredential(email, usernameChangePassword)
            currentUser?.reauthenticate(credential)?.await()

            val available = UserInformationRepositoryImpl.isUsernameAvailable(newUsername)
            if (!available) {
                usernameError = AuthError(context.getString(R.string.error_username_taken))
                return false
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
                true
            } else {
                usernameError = AuthError(
                    context.getString(R.string.error_update_username_failed, result.toString())
                )
                false
            }

        } catch (e: Exception) {
            usernameError = AuthError(
                context.getString(R.string.error_update_username_failed, e.message ?: "")
            )
            false
        } finally {
            updatingUsername = false
        }
    }

    // Change password
    suspend fun onChangePassword(context: Context): Boolean {
        if (!passwordChangeFormValid) {
            passwordError = AuthError(context.getString(R.string.error_invalid_passwords))
            return false
        }

        return try {
            val email = currentUser?.email ?: throw Exception("User email not available")
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            currentUser?.reauthenticate(credential)?.await()

            currentUser?.updatePassword(newPassword)?.await()

            currentPassword = ""
            newPassword = ""
            repeatNewPassword = ""
            passwordError = null
            true
        } catch (e: Exception) {
            passwordError = AuthError(
                context.getString(R.string.error_password_change_failed, e.message ?: "")
            )
            false
        } finally {
            updatingPassword = false
        }
    }

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
    fun onDeleteAccount(context: Context) {
        if (deletePassword.isEmpty()) {
            deleteAccountError = AuthError(context.getString(R.string.error_password_required))
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
                deleteAccountError = AuthError(
                    context.getString(R.string.error_account_deletion_failed, e.message ?: "")
                )
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
