package ui.screen.metrics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import domain.model.TimeSeriesPoint

@Composable
fun RequestCountChart(
    data: List<TimeSeriesPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(start = 40.dp, end = 40.dp, top = 20.dp, bottom = 20.dp)
        ) {
            val width = size.width
            val height = size.height

            // Calculate min and max values
            val maxValue = data.maxOfOrNull { it.value ?: 0.0 }?.toFloat()?.times(1.1f) ?: 0f
            val valueRange = maxValue.coerceAtLeast(1f)

            // Draw grid lines
            val gridColor = Color.LightGray.copy(alpha = 0.3f)
            val gridSteps = 5

            // Horizontal grid lines
            for (i in 0..gridSteps) {
                val y = height - (i.toFloat() / gridSteps) * height
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Draw bars
            val pointCount = data.size
            val barWidth = (width / pointCount) * 0.8f
            val barSpacing = (width / pointCount) * 0.2f

            data.forEachIndexed { index, point ->
                val x = index * (barWidth + barSpacing)
                val normalizedValue = (point.value?.toFloat()?.div(valueRange))
                val barHeight = normalizedValue?.times(height) ?: 0f

                drawRect(
                    color = primaryColor,
                    topLeft = Offset(x, height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        // Value and time labels using Compose Text elements
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Value labels on left side
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .padding(end = 4.dp)
            ) {
                val maxValue = data.maxOfOrNull { it.value ?: 0.0 }?.toFloat()?.times(1.1f) ?: 0f

                // Max value label
                Text(
                    text = "${maxValue.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 20.dp)
                )

                // Mid value label
                Text(
                    text = "${(maxValue / 2).toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Zero value label
                Text(
                    text = "0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 20.dp)
                )
            }

            // Time labels at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(start = 40.dp, end = 40.dp)
            ) {
                // Show selected time labels
                if (data.isNotEmpty()) {
                    // First time point
                    Text(
                        text = data.first().time.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // Middle time point if applicable
                    if (data.size > 2) {
                        Text(
                            text = data[data.size / 2].time.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Last time point
                    Text(
                        text = data.last().time.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}
