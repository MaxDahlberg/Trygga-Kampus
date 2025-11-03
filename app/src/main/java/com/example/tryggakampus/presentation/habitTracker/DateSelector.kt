package com.example.tryggakampus.presentation.habitTracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import com.example.tryggakampus.R

@Composable
fun DateSelector(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit
) {
    val currentLocale = LocalConfiguration.current.locales[0]
    val dayOfWeek = remember(selectedDate, currentLocale) { SimpleDateFormat("EEEE", currentLocale).format(selectedDate) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onDateSelected(DateUtils.addDays(selectedDate, -1)) }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous day"
            )
        }

        val weekOfYear = DateUtils.getWeekOfYear(selectedDate)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = DateUtils.formatDate(selectedDate),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text =  stringResource(R.string.week),
                style = MaterialTheme.typography.labelSmall
            )
        }

        IconButton(
            onClick = { onDateSelected(DateUtils.addDays(selectedDate, 1)) }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next day"
            )
        }

        TextButton(
            onClick = { onDateSelected(Date()) }
        ) {
            Text(stringResource(R.string.today), color = Color.Black)
        }
    }
}