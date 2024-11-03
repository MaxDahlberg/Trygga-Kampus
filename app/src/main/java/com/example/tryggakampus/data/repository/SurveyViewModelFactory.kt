package com.example.tryggakampus.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tryggakampus.data.SurveyRepository
import com.example.tryggakampus.presentation.surveyPage.SurveyViewModel


class SurveyViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurveyViewModel::class.java)) {
            // Create the repository here, no need to pass it in SurveyPage
            val repository = SurveyRepository()
            return SurveyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}