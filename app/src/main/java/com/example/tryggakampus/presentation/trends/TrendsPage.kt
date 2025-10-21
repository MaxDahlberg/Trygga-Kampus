package com.example.tryggakampus.presentation.trends

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.di.AppContainer
import com.example.tryggakampus.di.ViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrendsPage(title: String) {
    val viewModel: TrendsViewModel = viewModel(
        factory = ViewModelFactory(AppContainer.provideEvaluationRepository())
    )

    // Collect the raw evaluation data from the ViewModel
    val evaluations by viewModel.evaluations.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Self-Esteem Over Time",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Filter for evaluations that can be plotted
                val chartData = evaluations
                    .filter { it.selfEsteemScore != null && it.completedAt != null }
                    .sortedBy { it.completedAt }

                if (chartData.isNotEmpty()) {
                    // AndroidView is a special composable that lets us use traditional Android Views.
                    AndroidView(
                        factory = { context ->
                            // This block runs once to create the chart view
                            LineChart(context).apply {
                                // Basic chart styling
                                description.isEnabled = false
                                legend.isEnabled = false
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                xAxis.setDrawGridLines(false)
                                axisRight.isEnabled = false
                                axisLeft.axisMinimum = 0f // Y-axis starts at 0
                                axisLeft.axisMaximum = 11f // Y-axis ends at 11 (for a 1-10 scale)
                                setTouchEnabled(true)
                                setPinchZoom(true)
                            }
                        },
                        update = { chart ->
                            // This block runs whenever the chartData changes
                            val entries = chartData.map { evaluation ->
                                Entry(
                                    evaluation.completedAt!!.toDate().time.toFloat(),
                                    evaluation.selfEsteemScore!!.toFloat()
                                )
                            }

                            val dataSet = LineDataSet(entries, "Self-Esteem").apply {
                                // Line and point styling
                                color = Color.parseColor("#56A3A0") // App theme color
                                valueTextColor = Color.BLACK
                                setCircleColor(Color.parseColor("#56A3A0"))
                                circleRadius = 4f
                                setDrawCircleHole(false)
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                                setDrawFilled(true)
                                fillColor = Color.parseColor("#56A3A0")
                                fillAlpha = 60
                            }

                            // Format the X-axis to show dates instead of raw numbers
                            chart.xAxis.valueFormatter = object : ValueFormatter() {
                                private val sdf = SimpleDateFormat("M/d", Locale.getDefault())
                                override fun getFormattedValue(value: Float): String {
                                    return sdf.format(Date(value.toLong()))
                                }
                            }

                            chart.data = LineData(dataSet)
                            chart.invalidate() // Redraw the chart with new data
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Show a message if there's no data yet
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Complete a few evaluations to see your trends!")
                    }
                }
            }
        }
    }
}
