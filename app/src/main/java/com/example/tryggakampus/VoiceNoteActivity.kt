package com.example.tryggakampus

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tryggakampus.data.VoiceNoteRtdb
import com.example.tryggakampus.data.VoiceNoteRtdb.VoiceNoteMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.SystemClock
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ln
import kotlin.math.min

class VoiceNoteActivity : ComponentActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var outputFile: File
    private var recordStartMs: Long? = null
    private var lastSavedKey: String? = null
    private var lastSavedUid: String? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Audio permission required", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickAudioLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                handlePickedAudio(uri)
            }
        }

    private var notes: MutableState<List<VoiceNoteMeta>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prepare output file in cache
        outputFile = File(cacheDir, "voicenote.3gp")

        // Request permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        val activity = this@VoiceNoteActivity

        setContent {
            var recording by remember { mutableStateOf(false) }
            var playing by remember { mutableStateOf(false) }
            var analysisResult by remember { mutableStateOf<String?>(null) }
            val notesState = remember { mutableStateOf<List<VoiceNoteMeta>>(emptyList()) }
            notes = notesState

            // Rolling buffer of amplitudes for waveform
            val windowSize = 64
            val amplitudes = remember { mutableStateListOf<Float>().apply { repeat(windowSize) { add(0f) } } }

            // colorful brush for waveform
            val brush = remember {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF87CEEB), // sky blue
                        Color(0xFFFFFFE0), // light yellow
                        Color(0xFF4CAF50)  // grass green
                    )
                )
            }

            // Poll MediaRecorder max amplitude while recording and update waveform
            LaunchedEffect(recording) {
                if (recording) {
                    // small warm-up delay to ensure recorder has started
                    delay(60)
                }
                while (recording) {
                    val amp = activity.mediaRecorder?.maxAmplitude ?: 0
                    // Normalize amplitude [0,1] using log scale to make quiet sounds visible
                    val normalized = if (amp <= 0) 0f else (ln(amp.toFloat() + 1f) / ln(32768f)).coerceIn(0f, 1f)
                    // Smooth a bit by limiting delta
                    val last = amplitudes.lastOrNull() ?: 0f
                    val smoothed = when {
                        normalized > last -> min(last + 0.08f, normalized)
                        else -> last * 0.92f // decay
                    }
                    amplitudes.add(smoothed)
                    if (amplitudes.size > windowSize) amplitudes.removeAt(0)
                    delay(50)
                }
            }

            LaunchedEffect(Unit) {
                // initial load of notes list
                try {
                    val list = withContext(Dispatchers.IO) { VoiceNoteRtdb.fetchAllForCurrentUser() }
                    notesState.value = list
                } catch (_: Exception) {}
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Voice Note")
                Spacer(modifier = Modifier.height(12.dp))

                // Waveform visualizer with gradient
                AudioWaveform(
                    amplitudes = amplitudes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    barColor = Color.Unspecified,
                    barBrush = brush
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {
                    if (!recording) {
                        startRecording()
                        recording = true
                        analysisResult = null
                    } else {
                        stopRecording()
                        recording = false
                        // Auto-save immediately after stop
                        if (outputFile.exists()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val saved = withContext(Dispatchers.IO) { VoiceNoteRtdb.saveFileAsBase64(outputFile) }
                                    lastSavedKey = saved.key
                                    lastSavedUid = saved.uid
                                    Toast.makeText(this@VoiceNoteActivity, "Saved to RTDB", Toast.LENGTH_SHORT).show()
                                    // Refresh list after save
                                    try {
                                        val list = withContext(Dispatchers.IO) { VoiceNoteRtdb.fetchAllForCurrentUser() }
                                        notesState.value = list
                                    } catch (_: Exception) {}
                                } catch (e: Exception) {
                                    Toast.makeText(this@VoiceNoteActivity, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }) {
                    Text(if (!recording) "Start Recording" else "Stop Recording")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    if (!playing) {
                        startPlayback()
                        playing = true
                    } else {
                        stopPlayback()
                        playing = false
                    }
                }, enabled = outputFile.exists()) {
                    Text(if (!playing) "Play" else "Stop")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of saved notes
                Text("Saved notes:")
                Spacer(Modifier.height(6.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    items(notesState.value) { item ->
                        Button(onClick = {
                            // fetch by key and play
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val base64 = withContext(Dispatchers.IO) { VoiceNoteRtdb.fetchBase64ByKey(item.uid, item.key) }
                                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                                    val tmp = File.createTempFile("rtdb_play_", ".3gp", cacheDir)
                                    tmp.writeBytes(bytes)
                                    mediaPlayer?.release()
                                    mediaPlayer = MediaPlayer().apply {
                                        setDataSource(tmp.absolutePath)
                                        prepare(); start()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@VoiceNoteActivity, "Play failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = item.timestamp)
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {
                    if (outputFile.exists()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            analysisResult = "Analyzing..."
                            try {
                                val result = com.example.tryggakampus.VoiceAnalyzer.analyzeVoice(outputFile)
                                analysisResult = result
                            } catch (e: Exception) {
                                analysisResult = "Error: ${e.message}"
                            }
                        }
                    }
                }, enabled = outputFile.exists()) {
                    Text("Analyze Emotion")
                }

                Spacer(modifier = Modifier.height(16.dp))

                analysisResult?.let { res ->
                    Text(text = "Result: $res")
                }
            }
        }
    }

    private fun handlePickedAudio(uri: Uri) {
        try {
            val tmp = File.createTempFile("picked_", ".audio", cacheDir)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tmp).use { out ->
                    input.copyTo(out)
                }
            }
            // Analyze the picked file
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = com.example.tryggakampus.VoiceAnalyzer.analyzeVoice(tmp)
                    Toast.makeText(this@VoiceNoteActivity, result, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@VoiceNoteActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to read picked file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            recordStartMs = SystemClock.elapsedRealtime()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start recording: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop(); reset(); release()
            }
            mediaRecorder = null
            recordStartMs = null
            // No cloud save here. User will use the explicit "Save to Database" button.
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to stop recording: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startPlayback() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(outputFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener { mp ->
                    mp.reset()
                    mp.release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to play: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                stop()
                reset()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to stop playback: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun playFile(path: String) {
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
                setOnCompletionListener { mp -> mp.reset(); mp.release(); mediaPlayer = null }
                setOnErrorListener { mp, what, extra ->
                    Toast.makeText(this@VoiceNoteActivity, "Playback error ($what/$extra)", Toast.LENGTH_LONG).show()
                    mp.reset(); mp.release(); mediaPlayer = null
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to play: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer?.release()
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
