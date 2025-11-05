package com.example.tryggakampus.presentation.voice

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.VoiceNoteActivity

@Composable
fun VoiceNotePage() {
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Voice Note")

        Button(onClick = {
            val intent = Intent(ctx, VoiceNoteActivity::class.java)
            ctx.startActivity(intent)
        }) {
            Text(text = "Open Voice Note")
        }
    }
}

