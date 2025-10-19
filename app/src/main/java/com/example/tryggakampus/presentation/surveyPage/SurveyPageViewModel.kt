package com.example.tryggakampus.presentation.surveyPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.data.models.Evaluation
import com.example.tryggakampus.data.models.EvaluationType
import com.example.tryggakampus.data.repository.EvaluationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SurveyPageViewModel(
    private val evaluationRepository: EvaluationRepository
) : ViewModel() {

    val evaluationsHistory = evaluationRepository.getEvaluationsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun submitEvaluation(
        type: EvaluationType,
        answers: Map<String, Any>,
        selfEsteemScore: Int? = null
    ) {
        viewModelScope.launch {
            val newEvaluation = Evaluation(
                type = type,
                answers = answers,
                selfEsteemScore = selfEsteemScore
            )
            evaluationRepository.saveEvaluation(newEvaluation)
            // Add logic here to handle success or failure,
            // like navigating away or showing a toast message.
        }
    }
}
    