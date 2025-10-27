package com.example.tryggakampus.presentation.voice

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.io.File
import com.example.tryggakampus.VoiceAnalyzer
import com.example.tryggakampus.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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

    // Derived recording availability
    var hasRecording by remember { mutableStateOf(outputFile.exists() && outputFile.length() > 0) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show current proxy URL for quick diagnosis
        Text(text = "Proxy: ${BuildConfig.VOICE_PROXY_URL}")
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = if (recording) "Recording... ${elapsed}s" else "Voice Note")
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            scope.launch {
                val ping = VoiceAnalyzer.pingProxy()
                Log.d("VoiceNote", "Ping proxy -> $ping")
                Toast.makeText(ctx, ping, Toast.LENGTH_LONG).show()
            }
        }) { Text("Ping Proxy") }

        Spacer(modifier = Modifier.height(8.dp))

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
                // Stop recording
                try {
                    mediaRecorder?.apply {
                        stop()
                        reset()
                        release()
                    }
                    mediaRecorder = null
                    recording = false
                    timerJob?.cancel()
                    timerJob = null
                    // Recompute availability only after stop so the file is finalized
                    hasRecording = outputFile.exists() && outputFile.length() > 0
                    Log.d("VoiceNote", "Recording stopped. exists=${outputFile.exists()} len=${outputFile.length()}")
                } catch (e: Exception) {
                    Log.e("VoiceNote", "Failed to stop recording", e)
                    Toast.makeText(ctx, "Failed to stop recording: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }) {
            Text(if (!recording) "Start Recording" else "Stop Recording")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (!playing) {
                try {
                    if (!outputFile.exists() || outputFile.length() == 0L) {
                        Toast.makeText(ctx, "No recording found", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(outputFile.absolutePath)
                        prepare()
                        start()
                        setOnCompletionListener {
                            it.reset()
                            it.release()
                            mediaPlayer = null
                            playing = false
                        }
                    }
                    Log.d("VoiceNote", "Playback started: ${outputFile.length()} bytes")
                    playing = true
                } catch (e: Exception) {
                    Log.e("VoiceNote", "Failed to play", e)
                    Toast.makeText(ctx, "Failed to play: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                try {
                    mediaPlayer?.apply {
                        stop()
                        reset()
                        release()
                    }
                    mediaPlayer = null
                    playing = false
                    Log.d("VoiceNote", "Playback stopped")
                } catch (e: Exception) {
                    Log.e("VoiceNote", "Failed to stop playback", e)
                    Toast.makeText(ctx, "Failed to stop playback: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }, enabled = hasRecording && !recording) {
            Text(if (!playing) "Play" else "Stop")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (!outputFile.exists() || outputFile.length() == 0L) {
                Toast.makeText(ctx, "No recording to analyze", Toast.LENGTH_SHORT).show()
                return@Button
            }
            scope.launch {
                busy = true
                analysisResult = "Analyzing tone & words..."
                try {
                    val result = withContext(Dispatchers.IO) {
                        VoiceAnalyzer.analyzeVoice(outputFile)
                    }
                    analysisResult = result
                    Log.d("VoiceNote", "Analyze OK: $result")
                } catch (e: Exception) {
                    Log.e("VoiceNote", "Analyze failed", e)
                    analysisResult = "Error: ${e.message}"
                }
                busy = false
            }
        }, enabled = hasRecording && !busy && !recording) {
            Text("Analyze Emotion")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (busy) {
            CircularProgressIndicator()
        }

        analysisResult?.let { res ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Result: $res")
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
