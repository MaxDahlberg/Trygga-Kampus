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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.R
import com.example.tryggakampus.presentation.component.PageContainer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.max

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
        LegendToggle(stringResource(R.string.self_image), Color(0xFF1E88E5), visibility.value.first) {
            val v = visibility.value
            visibility.value = Triple(it, v.second, v.third)
        }
        LegendToggle(stringResource(R.string.self_esteem), Color(0xFFD81B60), visibility.value.second) {
            val v = visibility.value
            visibility.value = Triple(v.first, it, v.third)
        }
        LegendToggle(stringResource(R.string.self_reliance), Color(0xFF43A047), visibility.value.third) {
            val v = visibility.value
            visibility.value = Triple(v.first, v.second, it)
        }
    }
}

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
    // Ensure chronological order for drawing and axis calculations
    val sorted = remember(entries) { entries.sortedBy { it.date } }
    val dates = remember(sorted) { sorted.mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() } }
    val minDate = dates.first()
    val maxDate = dates.last()
    val spanDays = max(1f, java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate).toFloat())

    var tooltip by remember { mutableStateOf<Pair<Offset, String>?>(null) }

    // Layout paddings for axes and labels
    val leftPad = 40.dp
    val rightPad = 12.dp
    val topPad = 8.dp
    val bottomPad = 28.dp

    Column(Modifier
        .fillMaxWidth()
        .semantics { role = Role.Image; contentDescription = "Self-assessment chart: three series over time." }
    ) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .pointerInput(sorted) {
                detectTapGestures { offset ->
                    val w = size.width
                    val h = size.height
                    val plotLeft = leftPad.toPx()
                    val plotRight = w - rightPad.toPx()
                    val plotTop = topPad.toPx()
                    val plotBottom = h - bottomPad.toPx()
                    val plotWidth = plotRight - plotLeft

                    if (offset.x < plotLeft || offset.x > plotRight || offset.y < plotTop || offset.y > plotBottom) return@detectTapGestures

                    val x = offset.x
                    val day = (((x - plotLeft) / plotWidth) * spanDays).toInt().coerceAtLeast(0)
                    val date = minDate.plusDays(day.toLong()).coerceAtMost(maxDate)
                    val e = sorted.find { it.date == formatter.format(date) }
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

            val plotLeft = leftPad.toPx()
            val plotRight = w - rightPad.toPx()
            val plotTop = topPad.toPx()
            val plotBottom = h - bottomPad.toPx()
            val plotWidth = plotRight - plotLeft
            val plotHeight = plotBottom - plotTop

            // Grid lines and axes
            val gridColor = Color.LightGray.copy(alpha = 0.6f)
            // Y axis ticks 0..10 with labels
            for (i in 0..10) {
                val y = plotBottom - (i/10f)*plotHeight
                // horizontal grid
                drawLine(gridColor, start = Offset(plotLeft, y), end = Offset(plotRight, y), strokeWidth = 1f)
                // label
                val label = i.toString()
                drawIntoCanvas { canvas ->
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.sp.toPx()
                        isAntiAlias = true
                    }
                    val textWidth = textPaint.measureText(label)
                    canvas.nativeCanvas.drawText(
                        label,
                        plotLeft - 6.dp.toPx() - textWidth,
                        y + (textPaint.textSize / 2f) - 2.dp.toPx(),
                        textPaint
                    )
                }
            }
            // Y axis line
            drawLine(Color.Gray, start = Offset(plotLeft, plotTop), end = Offset(plotLeft, plotBottom), strokeWidth = 1.5f)
            // X axis line
            drawLine(Color.Gray, start = Offset(plotLeft, plotBottom), end = Offset(plotRight, plotBottom), strokeWidth = 1.5f)

            // X axis ticks and labels (up to ~6 labels)
            val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate).toInt().coerceAtLeast(0)
            val targetTicks = 6
            val step = max(1, daysBetween / targetTicks)
            val xLabelFmt = DateTimeFormatter.ofPattern("MMM d")
            var d = minDate
            while (!d.isAfter(maxDate)) {
                val x = plotLeft + (java.time.temporal.ChronoUnit.DAYS.between(minDate, d).toFloat() / spanDays) * plotWidth
                // tick
                drawLine(Color.Gray, start = Offset(x, plotBottom), end = Offset(x, plotBottom + 4.dp.toPx()), strokeWidth = 1f)
                // label
                val label = d.format(xLabelFmt)
                drawIntoCanvas { canvas ->
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 10.sp.toPx()
                        isAntiAlias = true
                    }
                    val textWidth = textPaint.measureText(label)
                    canvas.nativeCanvas.drawText(label, x - textWidth/2f, plotBottom + 14.dp.toPx(), textPaint)
                }
                d = d.plusDays(step.toLong())
            }
            // Ensure the last date label is present
            run {
                val x = plotLeft + (java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate).toFloat() / spanDays) * plotWidth
                val label = maxDate.format(xLabelFmt)
                drawIntoCanvas { canvas ->
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 10.sp.toPx()
                        isAntiAlias = true
                    }
                    val textWidth = textPaint.measureText(label)
                    canvas.nativeCanvas.drawText(label, x - textWidth/2f, plotBottom + 14.dp.toPx(), textPaint)
                }
            }

            fun xFor(date: LocalDate): Float {
                val days = java.time.temporal.ChronoUnit.DAYS.between(minDate, date).toFloat()
                return plotLeft + (days / spanDays) * plotWidth
            }
            fun yFor(value: Int?): Float? {
                if (value == null) return null
                return plotBottom - (value/10f) * plotHeight
            }

            val blue = Color(0xFF1E88E5)
            val pink = Color(0xFFD81B60)
            val green = Color(0xFF43A047)

            // Helper to build a smooth path through points (quadratic Bezier midpoint technique)
            fun buildSmoothPath(points: List<Offset>): Path {
                val path = Path()
                if (points.isEmpty()) return path
                if (points.size == 1) {
                    path.moveTo(points.first().x, points.first().y)
                    return path
                }
                path.moveTo(points.first().x, points.first().y)
                for (i in 0 until points.lastIndex) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val mid = Offset((p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
                    path.quadraticBezierTo(p0.x, p0.y, mid.x, mid.y)
                }
                // End to last
                val last = points.last()
                path.lineTo(last.x, last.y)
                return path
            }

            // Collect contiguous segments for each series (skip nulls)
            fun drawSeries(
                selector: (com.example.tryggakampus.data.model.SelfAssessment)->Int?,
                color: Color,
                shape: (Offset)->Unit
            ) {
                val segments = mutableListOf<MutableList<Offset>>()
                var current: MutableList<Offset>? = null
                sorted.forEach { e ->
                    val dte = LocalDate.parse(e.date, formatter)
                    val y = yFor(selector(e))
                    if (y != null) {
                        val p = Offset(xFor(dte), y)
                        if (current == null) {
                            current = mutableListOf()
                            segments.add(current!!)
                        }
                        current!!.add(p)
                        shape(p)
                    } else {
                        current = null
                    }
                }
                segments.forEach { seg ->
                    if (seg.size >= 2) {
                        val path = buildSmoothPath(seg)
                        drawPath(path, color = color, style = Stroke(width = 3f))
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
