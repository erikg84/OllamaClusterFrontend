package ui.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.MatrixThemeColors

@Composable
fun BulletPoint(
    text: String,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        // Bullet point
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (isActive) MatrixThemeColors.highlightColor else MatrixThemeColors.statusText.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Text
        Text(
            text = text,
            color = MatrixThemeColors.statusText,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}