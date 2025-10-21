package com.example.tryggakampus.presentation.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.data.models.Evaluation
import com.example.tryggakampus.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TrendsViewModel(
    private val evaluationRepository: EvaluationRepository
) : ViewModel() {

    // The ViewModel's only job is to provide the list of evaluations.
    // The UI will be responsible for turning this data into a chart.
    val evaluations: StateFlow<List<Evaluation>> = evaluationRepository.getEvaluationsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )
}
