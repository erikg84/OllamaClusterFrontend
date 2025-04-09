package ui.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.MatrixThemeColors

@Composable
fun ResourceUsageBar(
    label: String,
    value: Int,
    maxValue: Int
) {
    val percentage = (value.toFloat() / maxValue).coerceIn(0f, 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label
        Text(
            text = "$label: $value%",
            color = MatrixThemeColors.statusText,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(80.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .background(MatrixThemeColors.surface, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .background(MatrixThemeColors.highlightColor)
            )
        }
    }
}