@file:Suppress("unused", "UNUSED_PARAMETER")

package com.example.tryggakampus.presentation.profilePage

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tryggakampus.presentation.authentication.loginPage.AuthError

class TestProfileViewModel : ProfileViewModel() {

    override var username by mutableStateOf("Test User")
    override var email by mutableStateOf("test@example.com")
    override var hobbies by mutableStateOf(mutableSetOf<String>())
    override var allHobbies by mutableStateOf(listOf("Hobby1", "Hobby2"))
    override var hobbiesError by mutableStateOf<AuthError?>(null)
    override var usernameError by mutableStateOf<AuthError?>(null)
    override var passwordError by mutableStateOf<AuthError?>(null)
    override var deleteAccountError by mutableStateOf<AuthError?>(null)
    override var showRequestDataDialog by mutableStateOf(false)
    override var showDeleteAccountDialog by mutableStateOf(false)
    override var jsonData by mutableStateOf<String?>(null)

    // General test-friendly states (added to satisfy test helpers)
    var error by mutableStateOf<AuthError?>(null)
    var signingUp by mutableStateOf(false)

    // Username change
    override var usernameChangePassword by mutableStateOf("")
    override var usernameChangePasswordIsValid by mutableStateOf(true)
    override var newUsername by mutableStateOf("")
    override var newUsernameIsValid by mutableStateOf(true)
    override var isUsernameChangePasswordVisible by mutableStateOf(false)
    override var updatingUsername by mutableStateOf(false)

    // Password change
    override var currentPassword by mutableStateOf("")
    override var currentPasswordIsValid by mutableStateOf(true)
    override var newPassword by mutableStateOf("")
    override var newPasswordIsValid by mutableStateOf(true)
    override var repeatNewPassword by mutableStateOf("")
    override var isCurrentPasswordVisible by mutableStateOf(false)
    override var isNewPasswordVisible by mutableStateOf(false)
    override var isRepeatPasswordVisible by mutableStateOf(false)
    override var passwordChangeFormValid by mutableStateOf(false)
    override var updatingPassword by mutableStateOf(false)

    override var deletePassword by mutableStateOf("")
    var deletePasswordIsValid by mutableStateOf(true)
    var isDeletePasswordVisible by mutableStateOf(false)

    override fun onHobbyToggle(hobby: String) {
        if (hobbies.contains(hobby)) {
            hobbies.remove(hobby)
            hobbies = hobbies.toMutableSet()
        } else {
            hobbies.add(hobby)
            hobbies = hobbies.toMutableSet()
        }
    }

    override fun loadAllHobbies(context: Context) {
        allHobbies = listOf("Hobby1", "Hobby2", "Hobby3")
    }

    // Match super: suspend signature
    override suspend fun onSaveHobbies(context: Context): Boolean {
        delay(500)
        hobbiesError = null
        return true
    }

    // Match super: suspend signature
    override suspend fun onChangeUsername(context: Context): Boolean {
        delay(500)
        username = newUsername
        usernameError = null
        updatingUsername = false
        return true
    }

    override fun toggleUsernameChangePasswordVisibility() {
        isUsernameChangePasswordVisible = !isUsernameChangePasswordVisible
    }

    // use 'password' name to match supertype
    override fun onUsernameChangePasswordChange(password: String) {
        usernameChangePassword = password
        usernameChangePasswordIsValid = password.length >= 8
    }

    // Match super: suspend signature
    override suspend fun onChangePassword(context: Context): Boolean {
        delay(500)
        passwordError = null
        updatingPassword = false
        return true
    }

    override fun toggleCurrentPasswordVisibility() {
        isCurrentPasswordVisible = !isCurrentPasswordVisible
    }

    override fun onCurrentPasswordChange(password: String) {
        currentPassword = password
        currentPasswordIsValid = password.length >= 8
    }

    override fun toggleNewPasswordVisibility() {
        isNewPasswordVisible = !isNewPasswordVisible
    }

    // use 'password' name to match supertype
    override fun onNewPasswordChange(password: String) {
        newPassword = password
        newPasswordIsValid = password.length >= 8
    }

    override fun toggleRepeatPasswordVisibility() {
        isRepeatPasswordVisible = !isRepeatPasswordVisible
    }

    override fun onRequestData() {
        viewModelScope.launch {
            delay(500)
            jsonData = "{}"
        }
    }

    override fun onDeleteAccount(context: Context) {
        viewModelScope.launch {
            delay(500)
            deleteAccountError = null
            showDeleteAccountDialog = false
        }
    }

    override fun resetJsonData() {
        jsonData = null
    }

    override fun clearError() {
        error = null
        hobbiesError = null
        usernameError = null
        passwordError = null
        deleteAccountError = null
    }

    fun setErrorForTest(message: String?) {
        error = message?.let { AuthError(it) }
    }

    fun setSigningUpForTest(isSigning: Boolean) {
        signingUp = isSigning
    }

    fun setEmailForTest(value: String) {
        email = value
    }

    fun setPasswordForTest(value: String) {
        newPassword = value
        newPasswordIsValid = value.length >= 8
        passwordError = null
    }

    fun setHobbiesForTest(hobbiesSet: Set<String>) {
        this.hobbies = hobbiesSet.toMutableSet()
    }
}
