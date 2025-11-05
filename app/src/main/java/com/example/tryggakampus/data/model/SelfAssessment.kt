package com.example.tryggakampus.data.model

import com.google.firebase.Timestamp

/**
 * Firestore document for users/{uid}/selfAssessments/{YYYY-MM-DD}
 */
data class SelfAssessment(
    val uid: String = "",
    val date: String = "", // YYYY-MM-DD in user's local time zone
    val selfImage: Int? = null,
    val selfEsteem: Int? = null,
    val selfReliance: Int? = null,
    val note: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val tz: String = ""
)

fun SelfAssessment.isEmptyScores(): Boolean =
    selfImage == null && selfEsteem == null && selfReliance == null && (note.isNullOrBlank())

