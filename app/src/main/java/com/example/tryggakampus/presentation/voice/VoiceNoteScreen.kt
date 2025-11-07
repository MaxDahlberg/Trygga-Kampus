package com.example.tryggakampus.presentation.voice

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tryggakampus.VoiceAnalyzer
import com.example.tryggakampus.data.VoiceNoteRtdb
import com.example.tryggakampus.data.VoiceNoteRtdb.VoiceNoteMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.ln
import kotlin.math.min

@Suppress("DEPRECATION")
@Composable
fun VoiceNoteScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var recording by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    // Track elapsed seconds while recording for user feedback
    var elapsed by remember { mutableStateOf(0) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    val outputFile = remember { File(ctx.cacheDir, "voicenote.3gp") }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Keep last RTDB key/uid for playback
    var lastSavedKey by remember { mutableStateOf<String?>(null) }
    var lastSavedUid by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf<List<VoiceNoteMeta>>(emptyList()) }

    // Selection for analysis (can be a previously saved note temp file)
    var selectedForAnalysis by remember { mutableStateOf<File?>(null) }
    var selectedLabel by remember { mutableStateOf<String?>(null) }

    // Derived recording availability (re-add)
    var hasRecording by remember { mutableStateOf(outputFile.exists() && outputFile.length() > 0) }

    // colorful brush for waveform
    val brush = remember {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF87CEEB), // sky blue
                Color(0xFFFFFFE0), // light yellow
                Color(0xFF4CAF50)  // grass green
            )
        )
    }

    LaunchedEffect(Unit) {
        try { notes = withContext(Dispatchers.IO) { VoiceNoteRtdb.fetchAllForCurrentUser() } } catch (_: Exception) {}
    }

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        permissionGranted = granted
        if (!granted) {
            Toast.makeText(ctx, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            Log.w("VoiceNote", "RECORD_AUDIO permission denied by user")
        } else {
            Log.d("VoiceNote", "RECORD_AUDIO permission granted")
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Waveform state: rolling window of normalized amplitudes [0..1]
    val windowSize = 64
    val amplitudes = remember { mutableStateListOf<Float>().apply { repeat(windowSize) { add(0f) } } }

    // Poll getMaxAmplitude while recording and update waveform
    LaunchedEffect(recording) {
        if (recording) {
            delay(60)
        }
        while (recording) {
            val amp = mediaRecorder?.maxAmplitude ?: 0
            val normalized = if (amp <= 0) 0f else (ln(amp.toFloat() + 1f) / ln(32768f)).coerceIn(0f, 1f)
            val last = amplitudes.lastOrNull() ?: 0f
            val smoothed = if (normalized > last) min(last + 0.08f, normalized) else last * 0.92f
            amplitudes.add(smoothed)
            if (amplitudes.size > windowSize) amplitudes.removeAt(0)
            delay(50)
        }
        // gentle decay when stopped
        repeat(12) {
            val last = amplitudes.lastOrNull() ?: 0f
            amplitudes.add(last * 0.85f)
            if (amplitudes.size > windowSize) amplitudes.removeAt(0)
            delay(30)
        }
    }

    // Define playFile helper before it is used
    val playFile: (String) -> Unit = { path ->
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(path)
                setOnPreparedListener { it.start() }
                setOnCompletionListener { mp -> mp.reset(); mp.release(); mediaPlayer = null; playing = false }
                setOnErrorListener { mp, what, extra ->
                    Toast.makeText(ctx, "Playback error ($what/$extra)", Toast.LENGTH_LONG).show()
                    mp.reset(); mp.release(); mediaPlayer = null; playing = false
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(ctx, "Failed to play: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (recording) "Recording... ${elapsed}s" else "Voice Note")
        Spacer(modifier = Modifier.height(12.dp))

        // Waveform visualizer
        AudioWaveform(
            amplitudes = amplitudes,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            barColor = Color.Unspecified,
            barBrush = brush
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            if (!recording) {
                // Ensure permission
                if (!permissionGranted) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    return@Button
                }
                // Start recording
                try {
                    // Remove any prior incomplete file
                    if (outputFile.exists()) outputFile.delete()
                    mediaRecorder = MediaRecorder().apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        setOutputFile(outputFile.absolutePath)
                        prepare()
                        start()
                    }
                    Log.d("VoiceNote", "Recording started -> ${outputFile.absolutePath}")
                    recording = true
                    analysisResult = null
                    elapsed = 0
                    // When starting a new recording, clear previous selection to avoid confusion
                    selectedForAnalysis = null
                    selectedLabel = null
                    timerJob?.cancel()
                    timerJob = scope.launch {
                        while (true) {
                            delay(1000)
                            elapsed += 1
                        }
                    }
                } catch (e: Exception) {
                    Log.e("VoiceNote", "Failed to start recording", e)
                    Toast.makeText(ctx, "Failed to start recording: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                // Stop and auto-save
                try {
                    mediaRecorder?.apply { stop(); reset(); release() }; mediaRecorder = null
                    recording = false; timerJob?.cancel(); timerJob = null
                    hasRecording = outputFile.exists() && outputFile.length() > 0
                    scope.launch {
                        try {
                            val res = withContext(Dispatchers.IO) { VoiceNoteRtdb.saveFileAsBase64(outputFile) }
                            lastSavedKey = res.key; lastSavedUid = res.uid
                            notes = withContext(Dispatchers.IO) { VoiceNoteRtdb.fetchAllForCurrentUser() }
                            Toast.makeText(ctx, "Saved to RTDB", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Failed to stop recording: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }) { Text(if (!recording) "Start Recording" else "Stop Recording") }

        Spacer(Modifier.height(12.dp))

        Text("Saved notes:")
        Spacer(Modifier.height(6.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            items(notes) { item ->
                Button(onClick = {
                    scope.launch {
                        try {
                            val base64 = withContext(Dispatchers.IO) { VoiceNoteRtdb.fetchBase64ByKey(item.uid, item.key) }
                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                            val tmp = File.createTempFile("rtdb_play_", ".3gp", ctx.cacheDir)
                            tmp.writeBytes(bytes)
                            playFile(tmp.absolutePath)
                            playing = true
                            // Mark this file as selected for analysis
                            selectedForAnalysis = tmp
                            selectedLabel = item.timestamp
                            Toast.makeText(ctx, "Selected note for analysis", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Playback failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = item.timestamp)
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        // Show current selection for analysis if any
        if (selectedForAnalysis != null) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Selected for analysis: ${selectedLabel ?: selectedForAnalysis!!.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.TextButton(onClick = { selectedForAnalysis = null; selectedLabel = null }) {
                    Text("Clear")
                }
            }
        }

        // Analyze Emotion (restored)
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            // Prefer analyzing a selected saved note; else analyze current recording file
            val target: File? = selectedForAnalysis ?: outputFile.takeIf { it.exists() && it.length() > 0 }
            if (target == null) {
                Toast.makeText(ctx, "No audio selected or recorded", Toast.LENGTH_SHORT).show()
                return@Button
            }
            scope.launch {
                busy = true
                analysisResult = "Analyzing tone & words..."
                try {
                    val result = withContext(Dispatchers.IO) { VoiceAnalyzer.analyzeVoice(target) }
                    analysisResult = result
                } catch (e: Exception) {
                    analysisResult = "Error: ${e.message}"
                }
                busy = false
            }
        }, enabled = !busy && !recording) { Text("Analyze Emotion") }

        if (busy) {
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        analysisResult?.let { res ->
            Spacer(Modifier.height(12.dp))
            Text(text = "Analysis", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            androidx.compose.material3.Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 220.dp)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = res, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { mediaRecorder?.release() } catch (_: Exception) {}
            try { mediaPlayer?.release() } catch (_: Exception) {}
            timerJob?.cancel()
        }
    }
}

@Composable
private fun AudioWaveform(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF4CAF50),
    barBrush: Brush? = null
) {
    Canvas(modifier = modifier) {
        val n = amplitudes.size.coerceAtLeast(1)
        val barWidth = size.width / n
        val centerY = size.height / 2f
        val minBarHeight = size.height * 0.06f
        val maxBarHeight = size.height * 0.95f
        amplitudes.forEachIndexed { index, amp ->
            val h = (minBarHeight + (maxBarHeight - minBarHeight) * amp.coerceIn(0f, 1f))
            val left = index * barWidth
            val top = centerY - h / 2f
            val right = left + barWidth * 0.7f
            val bottom = centerY + h / 2f
            if (barBrush != null) {
                drawRect(
                    brush = barBrush,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(right - left, bottom - top)
                )
            } else {
                drawRect(
                    color = barColor,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(right - left, bottom - top)
                )
            }
        }
    }
}
