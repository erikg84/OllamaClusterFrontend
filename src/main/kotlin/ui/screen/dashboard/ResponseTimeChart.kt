package ui.screen.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import domain.model.TimeSeriesPoint

@Composable
fun ResponseTimeChart(
    data: List<TimeSeriesPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.primaryContainer

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val padding = 20f

        val effectiveWidth = width - 2 * padding
        val effectiveHeight = height - 2 * padding

        // Calculate min and max values
        val maxValue = data.maxOfOrNull { it.value ?: 0.0 }?.toFloat() ?: 0f
        val minValue = data.minOfOrNull { it.value ?: 0.0 }?.toFloat() ?: 0f
        val valueRange = (maxValue - minValue).coerceAtLeast(1f)

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

        // Vertical grid lines
        val pointCount = data.size
        for (i in 0 until pointCount) {
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
            color = secondaryColor.copy(alpha = 0.2f)
        )

        // Draw points
        data.forEachIndexed { index, point ->
            val x = padding + (index.toFloat() / (data.size - 1)) * effectiveWidth
            val normalizedValue = (point.value?.toFloat()?.minus(minValue))?.div(valueRange)
            val y = height - padding - (normalizedValue?.times(effectiveHeight) ?: 0f)

            drawCircle(
                color = primaryColor,
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}
