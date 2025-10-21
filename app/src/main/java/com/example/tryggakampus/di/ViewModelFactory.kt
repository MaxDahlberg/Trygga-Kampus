package com.example.tryggakampus.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tryggakampus.data.repository.EvaluationRepositoryImpl
import com.example.tryggakampus.domain.repository.EvaluationRepository
import com.example.tryggakampus.presentation.surveyPage.SurveyPageViewModel
import com.example.tryggakampus.presentation.trends.TrendsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


object AppContainer {
    fun provideEvaluationRepository(): EvaluationRepository {
        val auth = Firebase.auth
        val firestore = Firebase.firestore
        return EvaluationRepositoryImpl(auth, firestore)
    }
}

class ViewModelFactory(private val repository: EvaluationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurveyPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SurveyPageViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(TrendsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrendsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
