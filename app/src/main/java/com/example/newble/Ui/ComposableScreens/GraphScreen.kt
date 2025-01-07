package com.example.newble.Ui.ComposableScreens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun GraphScreen(
    temperatureRaw: Float,
    angleRaw: Float,
    rawData: List<Entry>,
    isRecording: Boolean,
    onSaveData: () -> Unit,
    isTemp: Boolean,
    toggleRecording: () -> Unit
) {
    val context = LocalContext.current

    val alpha = 0.1f // Adjust alpha to control the smoothing
    val filteredData = applyEwmaFilter(rawData, alpha)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = if (isTemp) "Temperature over time" else "Angle Over Time")
            LineChartView(context = context, data = filteredData.takeLast(1000), label = if (isTemp) "Temperature" else "Angle")

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = if (isTemp) "Temperature $temperatureRaw°C" else "Angle $angleRaw°")
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = toggleRecording,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = if (isRecording) "Stop recording" else "Start recording")
                }

                Button(
                    onClick = onSaveData,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "Save data")
                }
            }
        }
    }
}

fun applyEwmaFilter(rawData: List<Entry>, alpha: Float): List<Entry> {
    if (rawData.isEmpty()) return emptyList()

    val filteredData = mutableListOf<Entry>()
    var previousY = rawData.first().y

    rawData.forEach { entry ->
        val filteredY = alpha * entry.y + (1 - alpha) * previousY
        filteredData.add(Entry(entry.x, filteredY))
        previousY = filteredY
    }

    return filteredData
}

@Composable
fun LineChartView(context: Context, data: List<Entry>, label: String) {
    AndroidView(
        factory = {
            LineChart(context).apply {
                description = Description().apply { text = label }

                this.data = LineData(LineDataSet(data, label).apply {
                    lineWidth = 2f
                    setDrawCircles(false)
                })

                setDragEnabled(true)
                setScaleEnabled(true)
                isDoubleTapToZoomEnabled = true
                setPinchZoom(true)
            }
        },
        update = { chart ->
            val lineData = LineData(LineDataSet(data, label).apply {
                lineWidth = 2f
                setDrawCircles(false)
            })
            chart.data = lineData
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}