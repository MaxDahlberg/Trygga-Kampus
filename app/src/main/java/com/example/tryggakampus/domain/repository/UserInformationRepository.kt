package com.example.tryggakampus.domain.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlin.jvm.java
import com.example.tryggakampus.domain.model.UserInfoModel
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

interface UserInformationRepository {
    enum class RepositoryResult {
        SUCCESS,
        ERROR_NETWORK,
        ERROR_DATABASE,
        ERROR_UNKNOWN
    }

    suspend fun getUserInformation(userId: String, source: Source): Pair<RepositoryResult, UserInfoModel?>
    suspend fun addOrUpdateUserInformation(
        userInfo: UserInfoModel,
        updateFields: Map<String, Any>? = null
    ): RepositoryResult
    suspend fun isUsernameAvailable(username: String): Boolean
}

object UserInformationRepositoryImpl : UserInformationRepository {
    private const val COLLECTION_NAME = "user-information"

    override suspend fun getUserInformation(userId: String, source: Source): Pair<UserInformationRepository.RepositoryResult, UserInfoModel?> {
        val ref = Firebase.firestore.collection(COLLECTION_NAME).document(userId)
        return try {
            val snapshot = ref.get(source).await()
            val userInfo = snapshot.toObject(UserInfoModel::class.java)
            if (userInfo != null) {
                UserInformationRepository.RepositoryResult.SUCCESS to userInfo
            } else {
                UserInformationRepository.RepositoryResult.ERROR_DATABASE to null
            }
        } catch (e: FirebaseException) {
            Log.d("UserInfoRepository", "Error fetching user info: ${e.message}")
            UserInformationRepository.RepositoryResult.ERROR_NETWORK to null
        } catch (e: Exception) {
            Log.d("UserInfoRepository", "Unknown error: ${e.stackTraceToString()}")
            UserInformationRepository.RepositoryResult.ERROR_UNKNOWN to null
        }
    }

    override suspend fun addOrUpdateUserInformation(
        userInfo: UserInfoModel,
        updateFields: Map<String, Any>?
    ): UserInformationRepository.RepositoryResult {
        val ref = Firebase.firestore.collection(COLLECTION_NAME).document(userInfo.userId)

        return try {
            if (updateFields != null) {
                // Partial update
                ref.set(updateFields, SetOptions.merge()).await()
            } else {
                // Full replace
                ref.set(userInfo).await()
            }
            UserInformationRepository.RepositoryResult.SUCCESS
        } catch (e: FirebaseFirestoreException) {
            Log.d("UserInfoRepository", "Firestore error: ${e.message}")
            UserInformationRepository.RepositoryResult.ERROR_DATABASE
        } catch (e: Exception) {
            Log.d("UserInfoRepository", "Unknown error: ${e.stackTraceToString()}")
            UserInformationRepository.RepositoryResult.ERROR_UNKNOWN
        }
    }

    override suspend fun isUsernameAvailable(username: String): Boolean {
        return try {
            val querySnapshot = Firebase.firestore
                .collection(COLLECTION_NAME)
                .whereEqualTo("username", username)
                .get()
                .await()

            // Username is available if no documents exist with this username
            querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.d("UserInfoRepository", "Error checking username: ${e.message}")
            false // Consider unavailable on error
        }
    }
}