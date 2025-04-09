package ui.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domain.model.ChatMessage
import domain.model.MessageRole
import ui.MatrixThemeColors

@Composable
fun StaticChatMessage(
    message: ChatMessage,
    onCopyToClipboard: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    val messageColor = when (message.role) {
        MessageRole.USER -> MatrixThemeColors.userMessageBg
        MessageRole.ASSISTANT -> MatrixThemeColors.assistantMessageBg
        else -> MatrixThemeColors.surface
    }

    val textColor = when (message.role) {
        MessageRole.USER -> MatrixThemeColors.userMessageText
        MessageRole.ASSISTANT -> MatrixThemeColors.assistantMessageText
        else -> MatrixThemeColors.highlightColor.copy(alpha = 0.7f)
    }

    // Center the message boxes in the column
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Message content with Matrix-styled bubbles
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = if (message.role == MessageRole.USER) 8.dp else 2.dp,
                        bottomEnd = if (message.role == MessageRole.USER) 2.dp else 8.dp
                    )
                )
                .background(messageColor)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            clipboardManager.setText(AnnotatedString(message.content.orEmpty()))
                            onCopyToClipboard()
                        }
                    )
                }
                .padding(top = 12.dp, bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Text content - no animation, just display the full text
                Text(
                    text = message.content.orEmpty(),
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Copy button inside the message box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.content.orEmpty()))
                            onCopyToClipboard()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy to clipboard",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}