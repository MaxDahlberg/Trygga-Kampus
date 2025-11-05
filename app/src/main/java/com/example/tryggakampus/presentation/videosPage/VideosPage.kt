package com.example.tryggakampus.presentation.videosPage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.R
import com.example.tryggakampus.Routes

@Composable
fun VideosPage() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val videos = remember { mutableStateListOf<String>() }
    var duplicates by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showInfo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val names = try {
            context.assets.list("videos")?.toList().orEmpty()
                .map { it.trim() } // guard against stray whitespace
                .filter { it.endsWith(".mp4", ignoreCase = true) || it.endsWith(".mkv", ignoreCase = true) || it.endsWith(".webm", ignoreCase = true) }
        } catch (e: Exception) {
            emptyList()
        }
        val sorted = names.sorted()
        val dups = sorted.groupingBy { it.lowercase() }.eachCount().filter { it.value > 1 }
        Log.d("VideosPage", "Found ${sorted.size} videos: ${sorted}")
        if (dups.isNotEmpty()) Log.w("VideosPage", "Duplicate filenames (case-insensitive): ${dups}")
        videos.clear()
        videos.addAll(sorted)
        duplicates = dups
    }

    if (videos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Text(
                text = "No videos found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Videos",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
            )
            AssistChip(
                onClick = { showInfo = true },
                label = { Text(text = "${videos.size} items") }
            )
        }

        if (duplicates.isNotEmpty()) {
            Text(
                text = "Duplicates detected: ${duplicates.keys.joinToString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(videos) { name ->
                VideoTile(
                    title = name.substringBeforeLast('.')
                        .replace('-', ' ')
                        .replace('_', ' '),
                    onClick = {
                        navController.navigate(
                            Routes.VideosNavGraph.VideoPlayer(fileName = name)
                        )
                    }
                )
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Close") }
            },
            title = { Text("Discovered videos") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = videos.joinToString(separator = "\n"),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (duplicates.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Duplicates (case-insensitive):\n" + duplicates.entries.joinToString("\n") { "${it.key} -> ${it.value}x" },
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun VideoTile(title: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_articles_24),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(8.dp)
            )
        }
    }
}
