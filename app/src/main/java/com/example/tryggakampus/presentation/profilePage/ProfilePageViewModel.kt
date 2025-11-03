package com.example.tryggakampus.presentation.profilePage

import android.annotation.SuppressLint
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

@SuppressLint("MutableCollectionMutableState")
open class ProfileViewModel : ViewModel() {

    private val userRepo = UserInformationRepositoryImpl
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser
    private val currentUserId = currentUser?.uid ?: ""

    // Account info
    open var username by mutableStateOf("No username")
    open var email by mutableStateOf(currentUser?.email ?: "No email")

    // Change username
    open var newUsername by mutableStateOf("")
    open var usernameChangePassword by mutableStateOf("")
    open var updatingUsername by mutableStateOf(false)
    open var newUsernameIsValid by mutableStateOf(true)
    open var usernameChangePasswordIsValid by mutableStateOf(true)

    // Change password
    open var currentPassword by mutableStateOf("")
    open var newPassword by mutableStateOf("")
    open var repeatNewPassword by mutableStateOf("")
    open var updatingPassword by mutableStateOf(false)
    open var currentPasswordIsValid by mutableStateOf(true)
    open var newPasswordIsValid by mutableStateOf(true)

    open val passwordChangeFormValid: Boolean
        get() = currentPasswordIsValid &&
                newPasswordIsValid &&
                newPassword == repeatNewPassword &&
                newPassword.length >= 8

    // Password visibility toggles
    open var isUsernameChangePasswordVisible by mutableStateOf(false)
    open var isCurrentPasswordVisible by mutableStateOf(false)
    open var isNewPasswordVisible by mutableStateOf(false)
    open var isRepeatPasswordVisible by mutableStateOf(false)

    // Account deletion and user data
    open var deletePassword by mutableStateOf("")
    open var showDeleteAccountDialog by mutableStateOf(false)
    open var showRequestDataDialog by mutableStateOf(false)
    open var jsonData by mutableStateOf<String?>(null)

    // Errors
    open var usernameError by mutableStateOf<AuthError?>(null)
    open var passwordError by mutableStateOf<AuthError?>(null)
    open var hobbiesError by mutableStateOf<AuthError?>(null)
    open var deleteAccountError by mutableStateOf<AuthError?>(null)

    open var hobbies: MutableSet<String> by mutableStateOf(mutableSetOf())
    open var allHobbies by mutableStateOf<List<String>>(emptyList())
    open fun loadAllHobbies(context: Context) {
        allHobbies = HobbyList.allHobbies.map { HobbyList.getDisplayName(context, it.first) }
    }

    init {
        if (currentUserId.isNotEmpty()) {
            viewModelScope.launch {
                val (result, userInfo) = userRepo.getUserInformation(
                    currentUserId,
                    com.google.firebase.firestore.Source.SERVER
                )
                if (result == UserInformationRepository.RepositoryResult.SUCCESS && userInfo != null) {
                    username = userInfo.username ?: username
                    // Convert whatever type userInfo.hobbies is into a MutableSet
                    hobbies = when (val h = userInfo.hobbies) {
                        is List<*> -> h.filterIsInstance<String>().toMutableSet()
                        is Set<*> -> h.filterIsInstance<String>().toMutableSet()
                        else -> mutableSetOf()
                    }
                }
            }
        }
    }

    open fun onHobbyToggle(hobby: String) {
        val newSet = hobbies.toMutableSet()
        if (newSet.contains(hobby)) {
            newSet.remove(hobby)
        } else {
            newSet.add(hobby)
        }
        hobbies = newSet
    }

    open suspend fun onSaveHobbies(context: Context): Boolean {
        return try {
            val result = userRepo.addOrUpdateUserInformation(
                userInfo = UserInfoModel(userId = currentUserId),
                updateFields = mapOf("hobbies" to hobbies.toList())
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

    open suspend fun onChangeUsername(context: Context): Boolean {
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
            currentUser.reauthenticate(credential).await()

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

    open suspend fun onChangePassword(context: Context): Boolean {
        if (!passwordChangeFormValid) {
            passwordError = AuthError(context.getString(R.string.error_invalid_passwords))
            return false
        }

        return try {
            val email = currentUser?.email ?: throw Exception("User email not available")
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            currentUser.reauthenticate(credential).await()

            currentUser.updatePassword(newPassword).await()

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

    open fun onUsernameChangePasswordChange(password: String) {
        usernameChangePassword = password
        usernameChangePasswordIsValid = password.matches(passwordPattern)
    }

    open fun onCurrentPasswordChange(password: String) {
        currentPassword = password
        currentPasswordIsValid = password.matches(passwordPattern)
    }

    open fun onNewPasswordChange(password: String) {
        newPassword = password
        newPasswordIsValid = password.matches(passwordPattern)
    }

    open fun toggleUsernameChangePasswordVisibility() {
        isUsernameChangePasswordVisible = !isUsernameChangePasswordVisible
    }

    open fun toggleCurrentPasswordVisibility() {
        isCurrentPasswordVisible = !isCurrentPasswordVisible
    }

    open fun toggleNewPasswordVisibility() {
        isNewPasswordVisible = !isNewPasswordVisible
    }

    open fun toggleRepeatPasswordVisibility() {
        isRepeatPasswordVisible = !isRepeatPasswordVisible
    }

    open fun onDeleteAccount(context: Context) {
        if (deletePassword.isEmpty()) {
            deleteAccountError = AuthError(context.getString(R.string.error_password_required))
            return
        }

        viewModelScope.launch {
            try {
                val email = currentUser?.email ?: throw Exception("User email not available")
                val credential = EmailAuthProvider.getCredential(email, deletePassword)
                currentUser.reauthenticate(credential).await()

                currentUser.uid.let { GdprUserDataHelper().deleteUserData(it) }
                currentUser.delete().await()
                showDeleteAccountDialog = false
                deleteAccountError = null
            } catch (e: Exception) {
                deleteAccountError = AuthError(
                    context.getString(R.string.error_account_deletion_failed, e.message ?: "")
                )
            }
        }
    }

    open fun onRequestData() {
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
    open fun clearError() {
        hobbiesError = null
        usernameError = null
        passwordError = null
        deleteAccountError = null
    }

    open fun resetJsonData() {
        jsonData = null
    }
}
