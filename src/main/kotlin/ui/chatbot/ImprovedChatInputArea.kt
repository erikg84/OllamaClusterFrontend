package ui.chatbot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.MatrixThemeColors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImprovedChatInputArea(
    onMessageSent: (String) -> Unit,
    isGenerating: Boolean,
    streamingEnabled: Boolean = true,
    onStreamingToggled: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Handler for Enter key press
    val onEnterPressed: (String) -> Unit = { text ->
        if (text.isNotBlank() && !isGenerating) {
            onMessageSent(text)
            messageText = ""
        }
    }

    // Focus the text field on launch
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column {
        // Add streaming toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Typewriter Effect",
                color = MatrixThemeColors.statusText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = streamingEnabled,
                onCheckedChange = { onStreamingToggled() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MatrixThemeColors.highlightColor,
                    checkedTrackColor = MatrixThemeColors.accentColor,
                    uncheckedThumbColor = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                    uncheckedTrackColor = MatrixThemeColors.surface
                )
            )
        }

        // The rest of your input area code remains the same
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text input field with Matrix theme styling
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                TextField(
                    // Your existing TextField implementation
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Enter && !keyEvent.isShiftPressed && keyEvent.type == KeyEventType.KeyUp) {
                                onEnterPressed(messageText)
                                keyboardController?.hide()
                                true
                            } else {
                                false
                            }
                        },
                    placeholder = {
                        Text(
                            "Enter the Matrix...",
                            color = MatrixThemeColors.highlightColor.copy(alpha = 0.4f)
                        )
                    },
                    maxLines = 3,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MatrixThemeColors.inputFieldBg,
                        focusedContainerColor = MatrixThemeColors.inputFieldBg,
                        unfocusedTextColor = MatrixThemeColors.highlightColor,
                        focusedTextColor = MatrixThemeColors.highlightColor,
                        cursorColor = MatrixThemeColors.highlightColor,
                        focusedIndicatorColor = MatrixThemeColors.highlightColor.copy(alpha = 0.3f),
                        unfocusedIndicatorColor = MatrixThemeColors.highlightColor.copy(alpha = 0.1f)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            onEnterPressed(messageText)
                            keyboardController?.hide()
                        }
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy/Paste",
                            tint = MatrixThemeColors.highlightColor,
                            modifier = Modifier.clickable {
                                // Toggle between copy current text and paste
                                if (messageText.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(messageText))
                                } else {
                                    clipboardManager.getText()?.let { pastedText ->
                                        messageText = pastedText.text
                                    }
                                }
                            }
                        )
                    },
                    singleLine = false
                )
            }

            // Send button with Matrix styling
            Button(
                onClick = {
                    onEnterPressed(messageText)
                },
                enabled = messageText.isNotBlank() && !isGenerating,
                contentPadding = PaddingValues(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatrixThemeColors.buttonColor,
                    contentColor = MatrixThemeColors.highlightColor,
                    disabledContainerColor = MatrixThemeColors.buttonColor.copy(alpha = 0.3f),
                    disabledContentColor = MatrixThemeColors.highlightColor.copy(alpha = 0.3f)
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}