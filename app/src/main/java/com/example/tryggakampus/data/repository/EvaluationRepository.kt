package com.example.tryggakampus.domain.repository

import com.example.tryggakampus.data.models.Evaluation
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
    suspend fun saveEvaluation(evaluation: Evaluation): Result<Unit>
    fun getEvaluationsFlow(): Flow<List<Evaluation>>
    suspend fun getEvaluationsForDateRange(from: Timestamp, to: Timestamp): Result<List<Evaluation>>
}
