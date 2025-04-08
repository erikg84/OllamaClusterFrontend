package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domain.model.ChatMessage
import domain.model.MessageRole
import kotlinx.coroutines.launch
import viewmodel.InteractViewModel

// Matrix Theme Colors
private object MatrixThemeColors {
    val background = Color(0xFF000000) // Pure black
    val surface = Color(0xFF0D0D0D) // Nearly black
    val userMessageBg = Color(0xFF003B00) // Dark green
    val assistantMessageBg = Color(0xFF0F2318) // Darker green
    val userMessageText = Color(0xFF00FF00) // Bright matrix green
    val assistantMessageText = Color(0xFF4DFF4D) // Lighter matrix green
    val inputFieldBg = Color(0xFF0A1A0A) // Very dark green
    val buttonColor = Color(0xFF008F11) // Matrix code green
    val highlightColor = Color(0xFF00FF41) // Matrix highlight green
    val accentColor = Color(0xFF003B00) // Dark green accent
}

@Composable
fun ChatScreen(viewModel: InteractViewModel) {
    val chatMessages = viewModel.chatMessages
    val isGenerating by viewModel.isGenerating.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            scrollState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MatrixThemeColors.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Chat messages area (scrollable)
            Box(
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages) { message ->
                        ImprovedChatMessageItem(
                            message = message,
                            onCopyToClipboard = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Message copied to clipboard",
                                        withDismissAction = true)
                                }
                            }
                        )
                    }
                }

                // Loading indicator - use Matrix green
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        color = MatrixThemeColors.highlightColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input area (sticky at bottom)
            ImprovedChatInputArea(
                onMessageSent = { message ->
                    viewModel.addUserMessage(message)
                    coroutineScope.launch {
                        // Ensure scroll to bottom after message is added
                        scrollState.animateScrollToItem(chatMessages.size)
                    }
                },
                isGenerating = isGenerating
            )
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            snackbar = { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF323232),
                    contentColor = Color.White,
                    content = { Text(data.visuals.message) }
                )
            }
        )
    }
}

@Composable
fun ImprovedChatMessageItem(
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
                // Make the width match the parent width (same as input box)
                .fillMaxWidth(0.95f)  // Using 95% to match typical input box width
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
                // Only padding for top, bottom and right - horizontal padding will be added to text
                .padding(top = 12.dp, bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Text content with left alignment and horizontal padding
                Text(
                    text = message.content.orEmpty(),
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    textAlign = TextAlign.Start,  // Left-aligned text
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)  // 32dp horizontal spacing as requested
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

        // Add some spacing between messages
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImprovedChatInputArea(
    onMessageSent: (String) -> Unit,
    isGenerating: Boolean
) {
    var messageText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Handler for Enter key press
    val onEnterPressed: (String) -> Unit = { text ->
        if (text.isNotBlank() && !isGenerating) {
            clipboardManager.setText(AnnotatedString(text))
            onMessageSent(text)
            messageText = ""
        }
    }

    // Focus the text field on launch
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
                placeholder = { Text("Enter the Matrix...", color = MatrixThemeColors.highlightColor.copy(alpha = 0.4f)) },
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
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
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