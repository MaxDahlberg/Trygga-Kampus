package com.example.tryggakampus.presentation.selfassessment

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.R
import com.example.tryggakampus.presentation.component.PageContainer
import com.google.firebase.auth.FirebaseAuth
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfAssessmentPage(vm: SelfAssessmentViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        // Allow selecting any past date up to today (using UTC to match DatePicker's expectations)
        val todayUtc = LocalDate.now(ZoneOffset.UTC)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val d = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    return !d.isAfter(todayUtc)
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            vm.setSelectedDate(date)
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text(text = stringResource(id = android.R.string.ok)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(id = android.R.string.cancel), color = MaterialTheme.colorScheme.primary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; vm.deleteCurrent() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(id = android.R.string.cancel), color = MaterialTheme.colorScheme.primary)
                }
            },
            title = { Text(stringResource(R.string.self_assessment_delete)) },
            text = { Text("This will delete the entry for ${state.selectedDate}.") }
        )
    }

    if (selectedNote != null) {
        AlertDialog(
            onDismissRequest = { selectedNote = null },
            confirmButton = {
                Button(
                    onClick = { selectedNote = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text(stringResource(id = android.R.string.ok)) }
            },
            title = { Text("Note") },
            text = { Text(selectedNote!!) }
        )
    }

    PageContainer(
        modifier = Modifier
            .padding(12.dp)
    ) {
        val exportLabel = stringResource(R.string.export_csv)
        Text(text = stringResource(R.string.self_assessment_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        // Range controls
        RangeControls(state.range) { vm.setRange(it) }
        Spacer(Modifier.height(8.dp))

        // Date selector
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${state.selectedDate}")
            Spacer(Modifier.width(12.dp))
            OutlinedButton(onClick = { showDatePicker = true }) { Text(stringResource(id = R.string.select)) }
            Spacer(Modifier.weight(1f))
            if (state.hasPendingWrites) {
                AssistChip(onClick = {}, label = { Text(stringResource(R.string.self_assessment_saved_offline)) })
            }
        }

        Spacer(Modifier.height(12.dp))

        // Form sliders
        ScoreRow(
            label = stringResource(R.string.self_image),
            value = state.selfImage,
            onChange = { vm.setSelfImage(it) }
        )
        ScoreRow(
            label = stringResource(R.string.self_esteem),
            value = state.selfEsteem,
            onChange = { vm.setSelfEsteem(it) }
        )
        ScoreRow(
            label = stringResource(R.string.self_reliance),
            value = state.selfReliance,
            onChange = { vm.setSelfReliance(it) }
        )

        OutlinedTextField(
            value = state.note,
            onValueChange = { if (it.length <= 500) vm.setNote(it) },
            label = { Text(stringResource(R.string.self_assessment_notes_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "${state.note.length}/500", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { vm.save() }, enabled = !state.saving) {
                if (state.saving) CircularProgressIndicator(Modifier.size(16.dp)) else Text(stringResource(R.string.self_assessment_save))
            }
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.self_assessment_delete))
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                val csv = vm.generateCsv(state.entries)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "self_assessments.csv")
                    putExtra(Intent.EXTRA_TEXT, csv)
                }
                context.startActivity(Intent.createChooser(intent, exportLabel))
            }) { Text(exportLabel) }
        }
        if (state.savedMessage != null) {
            Text(text = state.savedMessage!!, color = MaterialTheme.colorScheme.primary)
        }
        if (state.error != null) {
            Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        // Chart
        Text(text = stringResource(R.string.self_assessment_history), style = MaterialTheme.typography.titleMedium)
        if (state.entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) { Text(stringResource(R.string.empty_state_no_data)) }
        } else {
            val legendVisibility = remember { mutableStateOf(Triple(true, true, true)) }
            Legend(legendVisibility)
            Spacer(Modifier.height(8.dp))
            TimeSeriesChart(
                entries = state.entries,
                showImage = legendVisibility.value.first,
                showEsteem = legendVisibility.value.second,
                showReliance = legendVisibility.value.third,
                onNoteClick = { selectedNote = it }
            )
        }
    }
}

@Composable
private fun RangeControls(selected: RangeOption, onChange: (RangeOption) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RangeOption.values().forEach { opt ->
            FilterChip(
                selected = opt == selected,
                onClick = { onChange(opt) },
                label = { Text(when(opt){ RangeOption.D7->"7d"; RangeOption.D30->"30d"; RangeOption.D90->"90d"; RangeOption.D365->"1y"; RangeOption.ALL->"All" }) }
            )
        }
    }
}

@Composable
private fun Legend(visibility: MutableState<Triple<Boolean, Boolean, Boolean>>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendToggle(stringResource(R.string.self_image), Color(0xFF1E88E5), visibility.value.first) { visibility.value = visibility.value.copy(first = it) }
        LegendToggle(stringResource(R.string.self_esteem), Color(0xFFD81B60), visibility.value.second) { visibility.value = visibility.value.copy(second = it) }
        LegendToggle(stringResource(R.string.self_reliance), Color(0xFF43A047), visibility.value.third) { visibility.value = visibility.value.copy(third = it) }
    }
}

private fun Triple<Boolean, Boolean, Boolean>.copy(first: Boolean = this.first, second: Boolean = this.second, third: Boolean = this.third) = Triple(first, second, third)

@Composable
private fun LegendToggle(label: String, color: Color, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier
        .semantics { role = Role.Checkbox; contentDescription = label }
        .clickable { onCheckedChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(14.dp).background(if (checked) color else color.copy(alpha = 0.3f), RoundedCornerShape(3.dp)))
        Spacer(Modifier.width(6.dp))
        Text(label)
    }
}

@Composable
private fun ScoreRow(label: String, value: Int?, onChange: (Int?) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = (value ?: 0).toFloat(),
                onValueChange = { onChange(it.toInt().coerceIn(0, 10).let { v -> if (v==0) null else v }) },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = (value ?: 0).toString())
        }
    }
}

@Composable
private fun TimeSeriesChart(
    entries: List<com.example.tryggakampus.data.model.SelfAssessment>,
    showImage: Boolean,
    showEsteem: Boolean,
    showReliance: Boolean,
    height: Dp = 220.dp,
    onNoteClick: (String) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val dates = remember(entries) { entries.mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() } }
    val minDate = dates.first()
    val maxDate = dates.last()
    val total = (kotlin.math.max(1L, java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate) + 1)).toFloat()

    var tooltip by remember { mutableStateOf<Pair<Offset, String>?>(null) }

    Column(Modifier
        .fillMaxWidth()
        .semantics { role = Role.Image; contentDescription = "Self-assessment chart: three series over time." }
    ) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .pointerInput(entries) {
                detectTapGestures { offset ->
                    // compute nearest date by x
                    val x = offset.x
                    val w = size.width
                    val day = ((x / w) * total).toInt().coerceAtLeast(0)
                    val date = minDate.plusDays(day.toLong()).coerceAtMost(maxDate)
                    val e = entries.find { it.date == formatter.format(date) }
                    if (e != null) {
                        val tip = "${e.date}: SI=${e.selfImage?:"-"}, SE=${e.selfEsteem?:"-"}, SR=${e.selfReliance?:"-"}${if (!e.note.isNullOrBlank()) " (📝)" else ""}"
                        tooltip = offset to tip
                        if (!e.note.isNullOrBlank()) onNoteClick(e.note!!)
                    }
                }
            }
        ) {
            val w = size.width
            val h = size.height
            // grid lines Y 0..10
            for (i in 0..10) {
                val y = h - (i/10f)*h
                drawLine(Color.LightGray, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
            }
            fun xFor(date: LocalDate): Float {
                val days = java.time.temporal.ChronoUnit.DAYS.between(minDate, date).toFloat()
                return (days / total) * w
            }
            fun yFor(value: Int?): Float? {
                if (value == null) return null
                return h - (value/10f) * h
            }
            val blue = Color(0xFF1E88E5)
            val pink = Color(0xFFD81B60)
            val green = Color(0xFF43A047)
            // Draw series lines with gaps
            fun drawSeries(selector: (com.example.tryggakampus.data.model.SelfAssessment)->Int?, color: Color, shape: (Offset)->Unit) {
                var last: Offset? = null
                entries.forEach { e ->
                    val d = LocalDate.parse(e.date, formatter)
                    val y = yFor(selector(e))
                    if (y != null) {
                        val x = xFor(d)
                        val p = Offset(x, y)
                        if (last != null) {
                            // connect consecutive non-null points
                            drawLine(color, start = last!!, end = p, strokeWidth = 3f)
                        }
                        shape(p)
                        last = p
                    } else {
                        last = null
                    }
                }
            }
            fun drawCircleAt(o: Offset, c: Color) = drawCircle(c, radius = 6f, center = o)
            fun drawSquareAt(o: Offset, c: Color) = drawRect(c, topLeft = Offset(o.x-6, o.y-6), size = androidx.compose.ui.geometry.Size(12f, 12f))
            fun drawDiamondAt(o: Offset, c: Color) {
                val path = Path().apply {
                    moveTo(o.x, o.y-8)
                    lineTo(o.x+8, o.y)
                    lineTo(o.x, o.y+8)
                    lineTo(o.x-8, o.y)
                    close()
                }
                drawPath(path, color = c)
            }
            if (showImage) drawSeries({ it.selfImage }, blue) { drawCircleAt(it, blue) }
            if (showEsteem) drawSeries({ it.selfEsteem }, pink) { drawDiamondAt(it, pink) }
            if (showReliance) drawSeries({ it.selfReliance }, green) { drawSquareAt(it, green) }
        }
        if (tooltip != null) {
            Spacer(Modifier.height(8.dp))
            Surface(shadowElevation = 2.dp, tonalElevation = 2.dp, shape = RoundedCornerShape(6.dp)) {
                Text(tooltip!!.second, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
