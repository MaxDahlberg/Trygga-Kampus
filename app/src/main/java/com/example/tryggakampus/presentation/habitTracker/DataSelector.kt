package com.example.tryggakampus.presentation.habitTracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.util.DateUtils
import java.util.*

@Composable
fun DateSelector(
    selectedDate: Date, // Changed from LocalDate to Date
    onDateSelected: (Date) -> Unit // Changed from LocalDate to Date
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

        Text(
            text = DateUtils.formatDate(selectedDate), // Use DateUtils instead of DateTimeFormatter
            style = MaterialTheme.typography.titleMedium
        )

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