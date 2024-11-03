package com.example.tryggakampus.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ErrorBox(message: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(shape = RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(vertical = 5.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.onErrorContainer)
    }
}