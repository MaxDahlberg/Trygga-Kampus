package com.example.tryggakampus.presentation.landingPage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.R
import com.example.tryggakampus.domain.model.UserInfoModel
import com.example.tryggakampus.domain.repository.UserInformationRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LandingPageViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUserId = firebaseAuth.currentUser?.uid ?: ""

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> get() = _username

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> get() = _usernameError

    private val _updatingUsername = MutableStateFlow(false)
    val updatingUsername: StateFlow<Boolean> get() = _updatingUsername

    init {
        fetchCurrentUsername()
    }

    private fun fetchCurrentUsername() {
        if (currentUserId.isEmpty()) return
        viewModelScope.launch {
            val (result, userInfo) = UserInformationRepositoryImpl.getUserInformation(
                currentUserId,
                com.google.firebase.firestore.Source.SERVER
            )
            if (result.name == "SUCCESS" && userInfo != null) {
                _username.value = userInfo.username
            } else {
                _username.value = null
            }
        }
    }

    fun updateUsername(newUsername: String, context: Context, onResult: (success: Boolean) -> Unit) {
        if (newUsername.isBlank()) {
            _usernameError.value = context.getString(R.string.username_empty)
            onResult(false)
            return
        }

        viewModelScope.launch {
            _updatingUsername.value = true
            val available = UserInformationRepositoryImpl.isUsernameAvailable(newUsername)
            if (!available) {
                _usernameError.value = context.getString(R.string.error_username_taken)
                _updatingUsername.value = false
                onResult(false)
                return@launch
            }

            val result = UserInformationRepositoryImpl.addOrUpdateUserInformation(
                userInfo = UserInfoModel(userId = currentUserId),
                updateFields = mapOf("username" to newUsername)
            )

            if (result.name == "SUCCESS") {
                _username.value = newUsername
                _usernameError.value = null
                _updatingUsername.value = false
                onResult(true)
            } else {
                _usernameError.value = context.getString(R.string.username_update_fail)
                _updatingUsername.value = false
                onResult(false)
            }
        }
    }
}
