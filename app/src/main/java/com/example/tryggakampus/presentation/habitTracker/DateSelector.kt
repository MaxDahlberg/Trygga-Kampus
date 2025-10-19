package com.example.tryggakampus.presentation.habitTracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.util.DateUtils
import java.util.Calendar
import java.util.Date

@Composable
fun DateSelector(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onDateSelected(DateUtils.addDays(selectedDate, -1)) } // Use DateUtils instead of minusDays
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack, // Fixed import
                contentDescription = "Previous day"
            )
        }

        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = DateUtils.formatDate(selectedDate), // Use DateUtils instead of DateTimeFormatter
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Week $weekOfYear",
                style = MaterialTheme.typography.labelSmall
            )
        }

        IconButton(
            onClick = { onDateSelected(DateUtils.addDays(selectedDate, 1)) } // Use DateUtils instead of plusDays
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward, // Fixed import
                contentDescription = "Next day"
            )
        }

        TextButton(
            onClick = { onDateSelected(Date()) } // Use Date() instead of LocalDate.now()
        ) {
            Text("Today")
        }
    }
}