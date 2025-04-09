package ui.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domain.model.LogEntry
import domain.model.LogLevel
import ui.MatrixThemeColors

@Composable
fun LogEntryRow(log: LogEntry) {
    val logColor = when (log.level) {
        LogLevel.ERROR -> MatrixThemeColors.errorColor
        LogLevel.WARN -> MatrixThemeColors.warningColor
        LogLevel.INFO -> MatrixThemeColors.infoColor
        LogLevel.DEBUG -> MatrixThemeColors.debugColor
        else -> MatrixThemeColors.statusText
    }

    val timestamp = log.timestamp?.substringAfter("T")?.substringBefore("Z") ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Log level indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(logColor, shape = RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Log content
        Column {
            // Time and source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timestamp,
                    color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = log.source ?: "",
                    color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Log message
            Text(
                text = log.message ?: "",
                color = logColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}