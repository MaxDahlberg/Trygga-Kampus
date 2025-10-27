package com.example.tryggakampus

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class VoiceNoteActivity : ComponentActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var outputFile: File

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prepare output file in cache
        outputFile = File(cacheDir, "voicenote.3gp")

        // Request permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            var recording by remember { mutableStateOf(false) }
            var playing by remember { mutableStateOf(false) }
            var analysisResult by remember { mutableStateOf<String?>(null) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Voice Note")
                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {
                    if (!recording) {
                        startRecording()
                        recording = true
                        analysisResult = null
                    } else {
                        stopRecording()
                        recording = false
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

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    if (outputFile.exists()) {
                        // Upload and analyze recorded audio
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

                Spacer(modifier = Modifier.height(12.dp))

                // New button: pick an audio file and analyze it
                Button(onClick = {
                    pickAudioLauncher.launch("audio/*")
                }) {
                    Text("Pick Audio File")
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
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start recording: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
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

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer?.release()
    }
}
