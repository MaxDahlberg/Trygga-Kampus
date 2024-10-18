package com.example.tryggakampus.presentation.surveyPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.data.SurveyRepository
import com.example.tryggakampus.domain.model.SurveyAnswer
import kotlinx.coroutines.launch

class SurveyViewModel(private val repository: SurveyRepository) : ViewModel() {

    val surveyRepository: SurveyRepository = repository

    fun submitSurvey(questions: List<String>, answers: List<String>) {
        viewModelScope.launch {
            val surveyAnswers = questions.mapIndexed { index, question  ->
                SurveyAnswer(question, answers[index])
            }
            repository.submitSurveyAnswers(surveyAnswers)
        }
    }
}
