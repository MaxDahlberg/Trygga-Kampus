package com.example.tryggakampus.data.repository

import com.example.tryggakampus.data.model.SelfAssessment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SelfAssessmentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun colPath(uid: String) = "users/$uid/selfAssessments"

    data class DocWithMeta(
        val doc: SelfAssessment?,
        val fromCache: Boolean,
        val hasPendingWrites: Boolean
    )

    fun observeRange(
        uid: String,
        startInclusive: LocalDate?,
        endInclusive: LocalDate?
    ): Flow<List<SelfAssessment>> = callbackFlow {
        var q: Query = db.collection(colPath(uid))
        if (startInclusive != null) {
            q = q.whereGreaterThanOrEqualTo("date", dateFmt.format(startInclusive))
        }
        if (endInclusive != null) {
            q = q.whereLessThanOrEqualTo("date", dateFmt.format(endInclusive))
        }
        q = q.orderBy("date", Query.Direction.ASCENDING)
        val reg = q.addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { it.toSelfAssessment() } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    fun observeDoc(uid: String, date: LocalDate): Flow<DocWithMeta> = callbackFlow {
        val id = dateFmt.format(date)
        val ref = db.document("${colPath(uid)}/$id")
        val reg = ref.addSnapshotListener { snap, _ ->
            val doc = if (snap != null && snap.exists()) snap.toSelfAssessment() else null
            val meta = snap?.metadata
            trySend(
                DocWithMeta(
                    doc = doc,
                    fromCache = meta?.isFromCache == true,
                    hasPendingWrites = meta?.hasPendingWrites() == true
                )
            )
        }
        awaitClose { reg.remove() }
    }

    suspend fun getByDate(uid: String, date: LocalDate): SelfAssessment? {
        val id = dateFmt.format(date)
        val doc = db.document("${colPath(uid)}/$id").get().await()
        return if (doc.exists()) doc.toSelfAssessment() else null
    }

    suspend fun upsert(
        uid: String,
        date: LocalDate,
        selfImage: Int?,
        selfEsteem: Int?,
        selfReliance: Int?,
        note: String?
    ) {
        val id = dateFmt.format(date)
        val ref = db.document("${colPath(uid)}/$id")
        val tz = ZoneId.systemDefault().id
        // Check local cache to decide if this looks like a create
        val inCacheExists = try { ref.get(Source.CACHE).await().exists() } catch (_: Exception) { false }
        val data = hashMapOf(
            "uid" to uid,
            "date" to id,
            "selfImage" to selfImage,
            "selfEsteem" to selfEsteem,
            "selfReliance" to selfReliance,
            "note" to (note?.take(500)),
            "tz" to tz,
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        if (!inCacheExists) {
            data["createdAt"] = FieldValue.serverTimestamp()
        }
        ref.set(data, SetOptions.merge()).await()
    }

    suspend fun delete(uid: String, date: LocalDate) {
        val id = dateFmt.format(date)
        db.document("${colPath(uid)}/$id").delete().await()
    }
}

private fun DocumentSnapshot.toSelfAssessment(): SelfAssessment? {
    val obj = this.toObject<SelfAssessment>() ?: return null
    // Ensure date id matches
    return obj.copy(date = this.id)
}
