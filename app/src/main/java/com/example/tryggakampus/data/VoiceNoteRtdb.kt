package com.example.tryggakampus.data

import android.util.Base64
import android.util.Log
import com.example.tryggakampus.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object VoiceNoteRtdb {

    data class SaveResult(
        val key: String,
        val uid: String
    )

    data class VoiceNoteMeta(
        val key: String,
        val uid: String,
        val timestamp: String
    )

    private suspend fun ensureAuthAndGetUid(): String = withContext(Dispatchers.IO) {
        val auth = FirebaseAuth.getInstance()
        val current = auth.currentUser
        if (current != null) return@withContext current.uid
        suspendCancellableCoroutine { cont ->
            auth.signInAnonymously()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
        return@withContext auth.currentUser?.uid
            ?: throw IllegalStateException("Anonymous sign-in failed")
    }

    private fun nowIso8601(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun dbRef() = try {
        val url = BuildConfig.RTDB_URL?.trim().orEmpty()
        if (url.isNotEmpty()) FirebaseDatabase.getInstance(url).reference
        else FirebaseDatabase.getInstance().reference
    } catch (_: Throwable) {
        FirebaseDatabase.getInstance().reference
    }

    suspend fun saveFileAsBase64(file: File): SaveResult = withContext(Dispatchers.IO) {
        val uid = ensureAuthAndGetUid()
        // Sometimes immediately after stop, file length may not be flushed; retry briefly
        var tries = 0
        var ok = file.exists() && file.length() > 0
        while (!ok && tries < 3) {
            Thread.sleep(80)
            ok = file.exists() && file.length() > 0
            tries++
        }
        if (!ok) throw IllegalArgumentException("Invalid audio file (empty) after retries")

        val bytes = try { file.readBytes() } catch (e: Exception) {
            throw IllegalStateException("Failed to read audio file: ${e.message}", e)
        }
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        val ref = dbRef().child("voice_notes").child(uid).push()
        val data = hashMapOf(
            "uid" to uid,
            "timestamp" to nowIso8601(),
            "voiceNoteBase64" to base64
        )
        suspendCancellableCoroutine<Unit> { cont ->
            ref.setValue(data)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(IllegalStateException("RTDB write failed: ${e.message}", e)) }
        }
        Log.d("VoiceNoteRtdb", "Saved note ${ref.key} (${bytes.size} bytes) for uid=$uid")
        return@withContext SaveResult(key = ref.key ?: "", uid = uid)
    }

    suspend fun fetchBase64ByKey(uid: String, key: String): String = withContext(Dispatchers.IO) {
        val ref = dbRef().child("voice_notes").child(uid).child(key).child("voiceNoteBase64")
        suspendCancellableCoroutine<String> { cont ->
            ref.get()
                .addOnSuccessListener { snap ->
                    val value = snap.getValue(String::class.java)
                    if (value != null && value.isNotEmpty()) cont.resume(value)
                    else cont.resumeWithException(IllegalStateException("No data at key or empty"))
                }
                .addOnFailureListener { e -> cont.resumeWithException(IllegalStateException("RTDB fetch failed: ${e.message}", e)) }
        }
    }

    suspend fun fetchAllForCurrentUser(): List<VoiceNoteMeta> = withContext(Dispatchers.IO) {
        val uid = ensureAuthAndGetUid()
        val ref = dbRef().child("voice_notes").child(uid)
        val snap: DataSnapshot = suspendCancellableCoroutine { cont ->
            ref.get()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { e -> cont.resumeWithException(IllegalStateException("RTDB list failed: ${e.message}", e)) }
        }
        val list = mutableListOf<VoiceNoteMeta>()
        for (child in snap.children) {
            val key = child.key ?: continue
            val ts = child.child("timestamp").getValue(String::class.java) ?: ""
            list += VoiceNoteMeta(key = key, uid = uid, timestamp = ts)
        }
        list.sortByDescending { it.timestamp }
        list
    }
}
