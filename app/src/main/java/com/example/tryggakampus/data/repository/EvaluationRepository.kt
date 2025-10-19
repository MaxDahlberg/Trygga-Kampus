package com.example.tryggakampus.domain.repositories

import com.example.tryggakampus.data.models.Evaluation
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
    suspend fun saveEvaluation(evaluation: Evaluation): Result<Unit>

    fun getEvaluationsFlow(): Flow<List<Evaluation>>

    suspend fun getEvaluationsForDateRange(from: androidx.compose.animation.graphics.vector.Timestamp, to: androidx.compose.animation.graphics.vector.Timestamp): Result<List<Evaluation>>
}
