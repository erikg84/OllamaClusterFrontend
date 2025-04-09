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
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import ui.MatrixThemeColors

@Composable
fun AnimatedChatMessage(
    message: ChatMessage,
    onCopyToClipboard: () -> Unit,
    onAnimationProgress: (Float) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    // State to track currently displayed text
    var displayedText by remember { mutableStateOf("") }

    // State for cursor blinking
    var showCursor by remember { mutableStateOf(false) }

    // Animate cursor blinking
    LaunchedEffect(Unit) {
        while (true) {
            showCursor = !showCursor
            delay(500) // Blink every 500ms
        }
    }

    // Typing animation effect
    LaunchedEffect(message.content) {
        if (message.role == MessageRole.ASSISTANT && !message.content.isNullOrEmpty()) {
            val fullText = message.content ?: ""
            displayedText = ""
            val charsToAnimate = fullText.length

            // Animate at a reasonable speed
            for (i in 1..charsToAnimate) {
                val partialText = fullText.take(i)
                displayedText = partialText

                // Calculate progress (0.0f to 1.0f)
                val progress = i.toFloat() / charsToAnimate.toFloat()
                onAnimationProgress(progress)

                // Typing speed based on message length
                val delay = when {
                    fullText.length > 500 -> 5L
                    fullText.length > 200 -> 8L
                    else -> 12L
                }
                delay(delay)
            }

            // Final progress update
            onAnimationProgress(1.0f)
        } else {
            // For user messages, just display immediately
            displayedText = message.content ?: ""
            onAnimationProgress(1.0f)
        }
    }

    // Rest of your existing message rendering code...
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

    // Is the message still typing?
    val isTyping = message.role == MessageRole.ASSISTANT &&
            message.content != null &&
            displayedText.length < message.content.length

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = displayedText,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        textAlign = TextAlign.Start
                    )

                    // Blinking cursor
                    if (isTyping && showCursor) {
                        Text(
                            text = "█",
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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