package com.example.tryggakampus.data

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object VoiceNoteDb {

    data class VoiceNoteSaveResult(
        val docId: String,
        val fileUrl: String?,
        val storagePath: String,
        val durationSeconds: Double
    )

    private suspend fun ensureAuthAndGetUid(): String = withContext(Dispatchers.IO) {
        val auth = FirebaseAuth.getInstance()
        val current = auth.currentUser
        if (current != null) return@withContext current.uid
        // Sign in anonymously when no user is present so rules with request.auth != null pass
        suspendCancellableCoroutine { cont ->
            auth.signInAnonymously()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
        return@withContext auth.currentUser?.uid
            ?: throw IllegalStateException("Anonymous sign-in failed")
    }

    /**
     * Legacy simple metadata save. Prefer [saveVoiceNoteWithFile].
     */
    suspend fun saveVoiceNote(durationSeconds: Double) = withContext(Dispatchers.IO) {
        val uid = ensureAuthAndGetUid()
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "userId" to uid,
            "duration" to durationSeconds,
            "timestamp" to FieldValue.serverTimestamp()
        )
        suspendCancellableCoroutine { cont ->
            db.collection("Voice_Audio_Note")
                .add(data)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }

    private fun getStorage(): FirebaseStorage {
        val app = try { FirebaseApp.getInstance() } catch (_: Exception) { null }
        val bucket = app?.options?.storageBucket
        val projectId = app?.options?.projectId ?: ""
        return when {
            !bucket.isNullOrBlank() && bucket.endsWith("firebasestorage.app", ignoreCase = true) -> {
                // google-services.json may contain firebasestorage.app for downloads; Storage expects the gs://<projectId>.appspot.com bucket id
                val alt = if (projectId.isNotBlank()) "gs://$projectId.appspot.com" else null
                Log.d("VoiceNoteDb", "Using explicit Storage bucket: ${alt ?: bucket}")
                if (alt != null) FirebaseStorage.getInstance(alt) else FirebaseStorage.getInstance()
            }
            !bucket.isNullOrBlank() -> {
                Log.d("VoiceNoteDb", "Using default Storage bucket from options: $bucket")
                FirebaseStorage.getInstance()
            }
            projectId.isNotBlank() -> {
                val alt = "gs://$projectId.appspot.com"
                Log.d("VoiceNoteDb", "Using derived Storage bucket: $alt")
                FirebaseStorage.getInstance(alt)
            }
            else -> FirebaseStorage.getInstance()
        }
    }

    /**
     * Upload the audio file to Firebase Storage and save metadata to Firestore.
     * Returns the created document id along with storage info. If Storage read rules prevent
     * obtaining a download URL, we still save the document with an empty recordingUrl.
     */
    suspend fun saveVoiceNoteWithFile(file: File, durationSeconds: Double): VoiceNoteSaveResult = withContext(Dispatchers.IO) {
        val uid = ensureAuthAndGetUid()
        if (!file.exists() || file.length() <= 0) throw IllegalArgumentException("Invalid audio file")

        val storage = getStorage()
        val storagePath = "voice_notes/$uid/${UUID.randomUUID()}.3gp"
        val ref = storage.reference.child(storagePath)

        // 1) Upload file with metadata
        val fileUri = android.net.Uri.fromFile(file)
        try {
            val meta = StorageMetadata.Builder()
                .setContentType("audio/3gpp")
                .setCustomMetadata("uid", uid)
                .setCustomMetadata("createdAt", System.currentTimeMillis().toString())
                .build()
            suspendCancellableCoroutine<Unit> { cont ->
                ref.putFile(fileUri, meta)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
        } catch (e: Exception) {
            val msg = if (e is StorageException) {
                val code = e.errorCode
                val http = e.cause?.message ?: e.message.orEmpty()
                "Upload failed (StorageException code=$code). If App Check is enforced, install Debug provider and register the debug token. Also verify Storage rules allow writes to voice_notes/{uid}/**. Raw: $http"
            } else {
                "Upload failed: ${e.message}"
            }
            throw RuntimeException(msg, e)
        }

        // 2) Try to get download URL (may fail if Storage read rules disallow)
        val recordingUrl: String? = try {
            suspendCancellableCoroutine<android.net.Uri> { cont ->
                ref.downloadUrl
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }.toString()
        } catch (_: Exception) {
            null
        }

        // 3) Save Firestore metadata
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "userId" to uid,
            "duration" to durationSeconds,
            "timestamp" to FieldValue.serverTimestamp(),
            "storagePath" to storagePath,
            "analysis_status" to "pending"
        ).apply {
            if (!recordingUrl.isNullOrBlank()) put("recordingUrl", recordingUrl)
            // keep legacy field for compatibility if URL exists
            if (!recordingUrl.isNullOrBlank()) put("fileUrl", recordingUrl)
        }

        val docId = suspendCancellableCoroutine<String> { cont ->
            db.collection("Voice_Audio_Note")
                .add(data)
                .addOnSuccessListener { cont.resume(it.id) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }

        VoiceNoteSaveResult(
            docId = docId,
            fileUrl = recordingUrl,
            storagePath = storagePath,
            durationSeconds = durationSeconds
        )
    }

    /**
     * Update an existing voice note document with analysis result text.
     */
    suspend fun updateVoiceNoteAnalysis(docId: String, analysis: String) = withContext(Dispatchers.IO) {
        val uid = ensureAuthAndGetUid()
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "analysis" to analysis,
            "analysis_status" to "done",
            "timestamp" to FieldValue.serverTimestamp() // keep updated timestamp
        )
        suspendCancellableCoroutine { cont ->
            db.collection("Voice_Audio_Note").document(docId)
                .update(data as Map<String, Any>)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }
}
