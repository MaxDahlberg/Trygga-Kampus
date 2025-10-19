package com.example.tryggakampus.data.repository

import androidx.compose.animation.graphics.vector.Timestamp
import com.example.tryggakampus.data.models.Evaluation
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
    suspend fun saveEvaluation(evaluation: Evaluation): Result<Unit>

    fun getEvaluationsFlow(): Flow<List<Evaluation>>

    suspend fun getEvaluationsForDateRange(from: Timestamp, to: Timestamp): Result<List<Evaluation>>
}
