package com.example.tryggakampus.domain.repository

import android.util.Log

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth

import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

sealed class AuthResponse {
    enum class SignIn {
        ERROR,
        SUCCESS,
        FAILURE
    }

    enum class SignUp {
        ERROR,
        SUCCESS,
        FAILURE,
        EMAIL_TAKEN
    }
}


interface AuthRepository {
    suspend fun authenticateUser(email: String, password: String): AuthResponse.SignIn
    suspend fun registerUser(email: String, password: String): AuthResponse.SignUp
}

object AuthRepositoryImpl: AuthRepository {
    override suspend fun authenticateUser(email: String, password: String): AuthResponse.SignIn {
        try {
            val result = Firebase.auth.signInWithEmailAndPassword(email, password).await()

            return (
                if (result.user == null)
                    AuthResponse.SignIn.FAILURE
                else
                    AuthResponse.SignIn.SUCCESS
            )
        }

        catch (e: FirebaseAuthInvalidCredentialsException) {
            return AuthResponse.SignIn.FAILURE
        }

        catch (e: FirebaseNetworkException) {
            Log.d("FATAL", e.stackTraceToString())
        }

        return AuthResponse.SignIn.ERROR
    }

    override suspend fun registerUser(email: String, password: String): AuthResponse.SignUp {
        try {
            val result = Firebase.auth.createUserWithEmailAndPassword(email, password).await()

            return (
                if (result.user == null)
                    AuthResponse.SignUp.FAILURE
                else
                    AuthResponse.SignUp.SUCCESS
            )
        }

        catch (e: FirebaseAuthUserCollisionException) {
            return AuthResponse.SignUp.EMAIL_TAKEN
        }

        catch (e: FirebaseAuthException) {
            Log.d("FATAL", e.stackTraceToString())
        }

        return AuthResponse.SignUp.ERROR
    }
}