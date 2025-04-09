package ui.chatbot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import domain.model.TimeSeriesPoint
import ui.MatrixThemeColors
import kotlin.math.max

@Composable
fun ImprovedResponseTimesChart(
    responseTimes: List<TimeSeriesPoint>
) {
    // Find min and max values
    val values = responseTimes.mapNotNull { it.value }
    val maxValue = values.maxOrNull() ?: 100.0
    val minValue = values.minOrNull() ?: 0.0
    val range = max(1.0, maxValue - minValue)

    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val pointWidth = width / (responseTimes.size - 1).coerceAtLeast(1)

            // Draw axis labels using the proper drawText API
            drawText(
                textMeasurer = textMeasurer,
                text = "${maxValue.toInt()}",
                topLeft = Offset(0f, 10f),
                style = TextStyle(
                    color = MatrixThemeColors.statusText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "${minValue.toInt()}",
                topLeft = Offset(0f, height - 15f),
                style = TextStyle(
                    color = MatrixThemeColors.statusText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            )

            // Draw grid lines
            val gridColor = MatrixThemeColors.separatorLine.copy(alpha = 0.3f)
            val gridSteps = 3
            for (i in 0..gridSteps) {
                val y = height * i / gridSteps
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 0.5f
                )
            }

            // Draw points and lines
            if (responseTimes.size > 1) {
                for (i in 0 until responseTimes.size - 1) {
                    val currentValue = responseTimes[i].value ?: continue
                    val nextValue = responseTimes[i + 1].value ?: continue

                    val x1 = i * pointWidth
                    val y1 = height - ((currentValue - minValue) / range * height).toFloat()
                    val x2 = (i + 1) * pointWidth
                    val y2 = height - ((nextValue - minValue) / range * height).toFloat()

                    // Draw line between points
                    drawLine(
                        color = MatrixThemeColors.chartLine,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2f
                    )

                    // Draw point
                    drawCircle(
                        color = MatrixThemeColors.chartLine,
                        radius = 3f,
                        center = Offset(x1, y1)
                    )
                }

                // Draw last point
                val lastIndex = responseTimes.size - 1
                val lastValue = responseTimes[lastIndex].value ?: return@Canvas
                val x = lastIndex * pointWidth
                val y = height - ((lastValue - minValue) / range * height).toFloat()

                drawCircle(
                    color = MatrixThemeColors.chartLine,
                    radius = 3f,
                    center = Offset(x, y)
                )
            } else if (responseTimes.size == 1) {
                // Draw single point
                val value = responseTimes[0].value ?: return@Canvas
                val y = height - ((value - minValue) / range * height).toFloat()

                drawCircle(
                    color = MatrixThemeColors.chartLine,
                    radius = 3f,
                    center = Offset(width / 2, y)
                )
            }
        }
    }
}