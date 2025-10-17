package com.example.tryggakampus.presentation.sober

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import java.time.LocalDate
import com.example.tryggakampus.R

@Composable
fun SoberActivityPage(title: String = "Sober") {
    val today = remember { LocalDate.now() }
    var checkedInToday by remember { mutableStateOf(false) }
    var streak by remember { mutableStateOf(1) }

    // ✅ Fix: LocalContext for resource access
    val context = LocalContext.current

    // ✅ Load challenges.json and tips.json from res/raw
    val challengesJson = remember {
        context.resources.openRawResource(R.raw.challenges)
            .bufferedReader().use { it.readText() }
    }
    val tipsJson = remember {
        context.resources.openRawResource(R.raw.tips)
            .bufferedReader().use { it.readText() }
    }

    // ✅ Parse JSON
    val challenges = remember { JSONObject(challengesJson).getJSONArray("challenges") }
    val tips = remember { JSONObject(tipsJson).getJSONArray("tips") }

    // ✅ Pick daily items
    val dailyChallenge = remember {
        challenges.getString(today.dayOfYear % challenges.length())
    }
    val dailyTip = remember {
        tips.getString(today.dayOfYear % tips.length())
    }

    // ---------- UI ----------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)

        // ✅ Streak Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Current streak: $streak day(s)", style = MaterialTheme.typography.titleLarge)
                Text("Last check-in: $today", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // ✅ Daily Tip Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("💡 Today's Tip", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(dailyTip, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // ✅ Daily Challenge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("🎯 Daily Challenge", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(dailyChallenge, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // ✅ Action Buttons
        Button(
            onClick = { checkedInToday = true; streak++ },
            enabled = !checkedInToday,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (checkedInToday) "Already checked in today" else "Check in for today")
        }

        OutlinedButton(
            onClick = { streak = 0; checkedInToday = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset streak")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Need support? Contact student services or national helplines.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
