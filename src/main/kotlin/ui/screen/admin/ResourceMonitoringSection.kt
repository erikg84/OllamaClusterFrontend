package ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint

@Composable
fun ResourceMonitoringSection(
    resourceTrends: Map<String, List<Double>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Resource Monitoring",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Resource gauges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // CPU Usage gauge
                ResourceGauge(
                    title = "CPU Usage",
                    value = resourceTrends["cpu"]?.lastOrNull() ?: 0.0,
                    color = MaterialTheme.colorScheme.primary
                )

                // Memory Usage gauge
                ResourceGauge(
                    title = "Memory Usage",
                    value = resourceTrends["memory"]?.lastOrNull() ?: 0.0,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Disk Usage gauge
                ResourceGauge(
                    title = "Disk Usage",
                    value = resourceTrends["disk"]?.lastOrNull() ?: 0.0,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resource trends chart
            Text(
                text = "Resource Trends (Last Hour)",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                ResourceTrendsChart(
                    resourceTrends = resourceTrends,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ResourceGauge(
    title: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = (value / 100).toFloat(),
                modifier = Modifier.size(80.dp),
                color = color,
                strokeWidth = 8.dp
            )

            Text(
                text = "${value.toInt()}%",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ResourceTrendsChart(
    resourceTrends: Map<String, List<Double>>,
    modifier: Modifier = Modifier
) {
    val cpuColor = MaterialTheme.colorScheme.primary
    val memoryColor = MaterialTheme.colorScheme.secondary
    val diskColor = MaterialTheme.colorScheme.tertiary

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val padding = 20f

            val effectiveWidth = width - 2 * padding
            val effectiveHeight = height - 2 * padding

            // Draw grid lines
            val gridColor = Color.LightGray.copy(alpha = 0.3f)
            val gridSteps = 4

            // Horizontal grid lines
            for (i in 0..gridSteps) {
                val y = padding + (i.toFloat() / gridSteps) * effectiveHeight
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1f
                )
            }

            // Maximum number of data points to expect
            val maxPoints = resourceTrends.values.maxOfOrNull { it.size } ?: 0

            // Draw trend lines for each resource
            resourceTrends.forEach { (resource, values) ->
                if (values.isEmpty()) return@forEach

                val color = when (resource) {
                    "cpu" -> cpuColor
                    "memory" -> memoryColor
                    "disk" -> diskColor
                    else -> Color.Gray
                }

                val linePath = Path()
                var firstPoint = true

                values.forEachIndexed { index, value ->
                    val x = padding + (index.toFloat() / (values.size - 1)) * effectiveWidth
                    val normalizedValue = (value / 100).toFloat()
                    val y = padding + (1 - normalizedValue) * effectiveHeight

                    if (firstPoint) {
                        linePath.moveTo(x, y)
                        firstPoint = false
                    } else {
                        linePath.lineTo(x, y)
                    }
                }

                // Draw the line
                drawPath(
                    path = linePath,
                    color = color,
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )

                // Draw points
                values.forEachIndexed { index, value ->
                    val x = padding + (index.toFloat() / (values.size - 1)) * effectiveWidth
                    val normalizedValue = (value / 100).toFloat()
                    val y = padding + (1 - normalizedValue) * effectiveHeight

                    drawCircle(
                        color = color,
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem(color = cpuColor, label = "CPU")
            LegendItem(color = memoryColor, label = "Memory")
            LegendItem(color = diskColor, label = "Disk")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
