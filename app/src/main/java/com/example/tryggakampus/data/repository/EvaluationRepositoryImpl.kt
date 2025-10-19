package com.example.tryggakampus.data.repository


import com.example.tryggakampus.data.models.Evaluation
import com.example.tryggakampus.domain.repository.EvaluationRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class EvaluationRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : EvaluationRepository {

    private val currentUser = auth.currentUser

    override suspend fun saveEvaluation(evaluation: Evaluation): Result<Unit> = try {
        if (currentUser == null) throw Exception("User not logged in")

        val userEvaluations = firestore.collection("user-information")
            .document(currentUser.uid)
            .collection("evaluations")

        userEvaluations.add(evaluation).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getEvaluationsFlow(): Flow<List<Evaluation>> {
        if (currentUser == null) return flowOf(emptyList())

        return firestore.collection("user-information")
            .document(currentUser.uid)
            .collection("evaluations")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Evaluation::class.java) }
    }

    override suspend fun getEvaluationsForDateRange(from: Timestamp, to: Timestamp): Result<List<Evaluation>> = try {
        if (currentUser == null) throw Exception("User not logged in")

        val querySnapshot = firestore.collection("user-information")
            .document(currentUser.uid)
            .collection("evaluations")
            .whereGreaterThanOrEqualTo("completedAt", from)
            .whereLessThanOrEqualTo("completedAt", to)
            .orderBy("completedAt", Query.Direction.ASCENDING)
            .get()
            .await()

        val evaluations = querySnapshot.toObjects(Evaluation::class.java)
        Result.success(evaluations)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
