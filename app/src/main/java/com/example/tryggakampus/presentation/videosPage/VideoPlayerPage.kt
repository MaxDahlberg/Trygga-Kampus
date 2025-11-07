package com.example.tryggakampus.presentation.videosPage

import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import java.util.Locale

@Composable
fun VideoPlayerPage(assetPath: String) {
    val context = LocalContext.current

    // Raw path used for AssetManager lookups (must NOT be URL-encoded)
    val rawPath = remember(assetPath) { assetPath.trimStart('/') }
    // Encoded path for URIs to safely handle spaces and non-ASCII
    val encodedPath = remember(rawPath) {
        rawPath.split('/').joinToString("/") { Uri.encode(it) }
    }

    // Quick existence check to give immediate feedback if the asset path is wrong
    val assetExists by remember(rawPath) {
        mutableStateOf(
            try {
                context.assets.open(rawPath).use { /* verify */ }
                true
            } catch (_: Exception) {
                false
            }
        )
    }

    var playbackError by remember { mutableStateOf<String?>(null) }
    var hasEnded by remember { mutableStateOf(false) }

    val player = remember(context, encodedPath) {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("asset:///" + encodedPath)
            val builder = MediaItem.Builder().setUri(uri)
            if (rawPath.endsWith(".mp4", ignoreCase = true)) {
                builder.setMimeType(MimeTypes.VIDEO_MP4)
            } else if (rawPath.endsWith(".webm", ignoreCase = true)) {
                builder.setMimeType(MimeTypes.VIDEO_WEBM)
            }
            setMediaItem(builder.build())
            addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    playbackError = error.localizedMessage ?: error.message
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        hasEnded = true
                    }
                }
            })
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    // Feedback questions for this asset (if any)
    val fileName = remember(rawPath) { rawPath.substringAfterLast('/') }
    val questions = remember(fileName) { feedbackQuestionsForFile(fileName) }

    // Text states for answers (one per question)
    val answerStates = remember(questions) { questions.map { mutableStateOf(TextFieldValue("")) } }

    // Prefill answers if already saved
    LaunchedEffect(questions, fileName) {
        val uid = Firebase.auth.currentUser?.uid ?: return@LaunchedEffect
        if (questions.isEmpty()) return@LaunchedEffect
        try {
            val snap = Firebase.firestore
                .collection("users").document(uid)
                .collection("videoFeedback").document(slugify(fileName))
                .get()
                .await()
            if (snap.exists()) {
                val saved = (snap.get("answers") as? List<*>)?.mapNotNull { it as? Map<*, *> }
                saved?.forEachIndexed { idx, _ ->
                    if (idx < answerStates.size) {
                        val text = (saved[idx]["answer"] as? String).orEmpty()
                        answerStates[idx].value = TextFieldValue(text)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    // Save status
    var saveStatus by remember { mutableStateOf<SaveStatus>(SaveStatus.Idle) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!assetExists) {
            AndroidView(
                factory = {
                    TextView(it).apply {
                        text = "Asset not found: $rawPath"
                        setTextColor(android.graphics.Color.WHITE)
                        textSize = 16f
                        setBackgroundColor(0x99000000.toInt())
                        layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        gravity = android.view.Gravity.CENTER
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = {
                        PlayerView(it).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                            useController = true
                            this.player = player
                            keepScreenOn = true
                        }
                    },
                    update = { it.player = player },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Show feedback when video finished AND we have questions for this video
                if (questions.isNotEmpty()) {
                    FeedbackSection(
                        visible = hasEnded,
                        title = "What did you think?",
                        questions = questions,
                        answers = answerStates,
                        saveStatus = saveStatus,
                        onSave = {
                            val realUid = Firebase.auth.currentUser?.uid ?: run {
                                saveStatus = SaveStatus.Error("Please sign in to submit feedback.")
                                return@FeedbackSection
                            }
                            saveStatus = SaveStatus.Saving
                            val payload = mapOf(
                                "uid" to realUid,
                                "videoId" to slugify(fileName),
                                "fileName" to fileName,
                                "assetPath" to rawPath,
                                "answers" to questions.mapIndexed { idx, q ->
                                    mapOf("question" to q, "answer" to answerStates[idx].value.text)
                                },
                                "updatedAt" to FieldValue.serverTimestamp(),
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                            val doc = Firebase.firestore
                                .collection("users").document(realUid)
                                .collection("videoFeedback").document(slugify(fileName))

                            doc.set(payload).addOnSuccessListener {
                                saveStatus = SaveStatus.Saved
                            }.addOnFailureListener { e ->
                                saveStatus = SaveStatus.Error(e.localizedMessage ?: "Failed to save")
                            }
                        }
                    )
                }
            }

            if (playbackError != null) {
                AndroidView(
                    factory = {
                        TextView(it).apply {
                            text = "Playback error: ${playbackError}"
                            setTextColor(android.graphics.Color.WHITE)
                            textSize = 14f
                            setBackgroundColor(0x99000000.toInt())
                            layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
                            gravity = android.view.Gravity.CENTER_HORIZONTAL
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FeedbackSection(
    visible: Boolean,
    title: String,
    questions: List<String>,
    answers: List<MutableState<TextFieldValue>>,
    saveStatus: SaveStatus,
    onSave: () -> Unit
) {
    if (!visible) return

    Surface(tonalElevation = 2.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            questions.forEachIndexed { idx, q ->
                Text(text = q, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = answers[idx].value,
                    onValueChange = { answers[idx].value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    minLines = 3
                )
                Spacer(Modifier.height(12.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onSave, enabled = saveStatus !is SaveStatus.Saving) {
                    if (saveStatus is SaveStatus.Saving) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Saving…")
                    } else {
                        Text("Save feedback")
                    }
                }
                Spacer(Modifier.width(12.dp))
                when (saveStatus) {
                    is SaveStatus.Saved -> Text("Saved", color = MaterialTheme.colorScheme.primary)
                    is SaveStatus.Error -> Text(
                        text = saveStatus.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    else -> {}
                }
            }
        }
    }
}

private sealed interface SaveStatus {
    data object Idle : SaveStatus
    data object Saving : SaveStatus
    data object Saved : SaveStatus
    data class Error(val message: String) : SaveStatus
}

// Map file name to feedback questions. Extendable for other videos later.
private fun feedbackQuestionsForFile(fileName: String): List<String> {
    val stem = fileName.substringBeforeLast('.')
    val slug = slugify(stem)
    return when (slug) {
        "verkligheten" -> listOf(
            "When you or someone you care about has struggled with mental health, what was the biggest barrier to getting help ?",
            "Did any personal stories or examples shared in the video reflect a time when you personally felt isolated or misunderstood due to mental health challenges?"
        )
        "self-esteem" -> listOf(
            "Can you recall when you felt you are not enough ?",
            "What is one piece of advice from the video that you wish someone had told you during a time when you were struggling with feeling 'not good enough'?"
        )
        "psykisk-ohalsa" -> listOf(
            "When you or someone you care about has struggled with mental health, what was the biggest barrier to getting help ?",
            "Did any personal stories or examples shared in the video reflect a time when you personally felt isolated or misunderstood due to mental health challenges ?"
        )
        "party-drugs" -> listOf(
            "Have you or someone you've been with ever been in a social setting where the risks mentioned in the video became a real concern?",
            "If someone offers you a drug; which specific fact about health risks or legal consequences from the video would be most likely to make you decline?"
        )
        "abuse" -> listOf(
            "Has there been a time when you or someone you know felt uncomfortable or suspicious about an interaction that later made you think about the grooming tactics mentioned in the video?",
            "if you were in a situation where you had to quickly exit an online conversation, which specific step from the video would you most likely remember and use ?"
        )
        else -> emptyList()
    }
}

private fun slugify(fileName: String): String {
    val base = fileName.substringBeforeLast('.')
    val normalized = Normalizer.normalize(base, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
    return normalized
        .lowercase(Locale.ROOT)
        .replace("[^a-z0-9]+".toRegex(), "-")
        .trim('-')
}
