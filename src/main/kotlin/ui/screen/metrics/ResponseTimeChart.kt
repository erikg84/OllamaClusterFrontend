package ui.screen.metrics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import domain.model.TimeSeriesPoint

@Composable
fun ResponseTimeChart(
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
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val padding = 40f

            val effectiveWidth = width - 2 * padding
            val effectiveHeight = height - 2 * padding

            // Calculate min and max values
            val maxValue = data.maxOfOrNull { it.value ?: 0.0 }?.toFloat() ?: 0f
            val minValue = data.minOfOrNull { it.value ?: 0.0 }?.toFloat()?.coerceAtMost(maxValue * 0.5f) ?: 0f
            val valueRange = (maxValue - minValue).coerceAtLeast(1f)

            // Draw grid lines
            val gridColor = Color.LightGray.copy(alpha = 0.3f)
            val gridSteps = 5

            // Horizontal grid lines
            for (i in 0..gridSteps) {
                val y = height - padding - (i.toFloat() / gridSteps) * effectiveHeight
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1f
                )
            }

            // Vertical grid lines
            val pointCount = data.size
            val skipFactor = if (pointCount > 12) pointCount / 6 else 1

            for (i in 0 until pointCount step skipFactor) {
                val x = padding + (i.toFloat() / (pointCount - 1)) * effectiveWidth
                drawLine(
                    color = gridColor,
                    start = Offset(x, padding),
                    end = Offset(x, height - padding),
                    strokeWidth = 1f
                )
            }

            // Draw line
            val linePath = Path()
            var firstPoint = true

            data.forEachIndexed { index, point ->
                val x = padding + (index.toFloat() / (data.size - 1)) * effectiveWidth
                val normalizedValue = (point.value?.toFloat()?.minus(minValue))?.div(valueRange)
                val y = height - padding - (normalizedValue?.times(effectiveHeight) ?: 0f)

                if (firstPoint) {
                    linePath.moveTo(x, y)
                    firstPoint = false
                } else {
                    linePath.lineTo(x, y)
                }
            }

            // Draw the line path
            drawPath(
                path = linePath,
                color = primaryColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )

            // Draw area under the line
            val areaPath = Path()
            areaPath.addPath(linePath)

            // Close the path to create an area
            areaPath.lineTo(padding + effectiveWidth, height - padding)
            areaPath.lineTo(padding, height - padding)
            areaPath.close()

            // Draw the area with a gradient
            drawPath(
                path = areaPath,
                color = primaryColor.copy(alpha = 0.1f)
            )

            // Draw points
            data.forEachIndexed { index, point ->
                val x = padding + (index.toFloat() / (data.size - 1)) * effectiveWidth
                val normalizedValue = (point.value?.toFloat()?.minus(minValue))?.div(valueRange)
                val y = height - padding - (normalizedValue?.times(effectiveHeight) ?: 0f)

                drawCircle(
                    color = primaryColor,
                    radius = 4f,
                    center = Offset(x, y)
                )
            }
        }

        // Value and time labels using Compose Text elements outside Canvas
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Value labels on left
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .padding(end = 4.dp)
            ) {
                val maxValue = data.maxOfOrNull { it.value ?: 0.0 }?.toFloat() ?: 0f
                val minValue = data.minOfOrNull { it.value ?: 0.0 }?.toFloat()?.coerceAtMost(maxValue * 0.5f) ?: 0f

                // Max value label
                Text(
                    text = "${maxValue.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 40.dp)
                )

                // Mid value label
                Text(
                    text = "${((maxValue + minValue) / 2).toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Min value label
                Text(
                    text = "${minValue.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 40.dp)
                )
            }

            // Time labels at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(start = 40.dp, end = 40.dp)
            ) {
                // Show first time point
                if (data.isNotEmpty()) {
                    Text(
                        text = data.first().time.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // Show middle time point
                    if (data.size > 2) {
                        Text(
                            text = data[data.size / 2].time.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Show last time point
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
