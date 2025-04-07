package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domain.model.ChatMessage
import domain.model.MessageRole
import kotlinx.coroutines.launch
import viewmodel.InteractViewModel

@Composable
fun ChatScreen(viewModel: InteractViewModel) {
    val chatMessages = viewModel.chatMessages
    val isGenerating by viewModel.isGenerating.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            scrollState.animateScrollToItem(chatMessages.size - 1)
        }
    }

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
                    ChatMessageItem(message)
                }
            }

            // Loading indicator
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input area (sticky at bottom)
        ChatInputArea(
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
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val clipboardManager = LocalClipboardManager.current
    val messageColor = when (message.role) {
        MessageRole.USER -> MaterialTheme.colorScheme.primaryContainer
        MessageRole.ASSISTANT -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when (message.role) {
        MessageRole.USER -> MaterialTheme.colorScheme.onPrimaryContainer
        MessageRole.ASSISTANT -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.role == MessageRole.USER) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (message.role == MessageRole.USER) 12.dp else 4.dp,
                        bottomEnd = if (message.role == MessageRole.USER) 4.dp else 12.dp
                    )
                )
                .background(messageColor)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            clipboardManager.setText(AnnotatedString(message.content.orEmpty()))
                        }
                    )
                }
                .padding(12.dp)
        ) {
            Text(
                text = message.content.orEmpty(),
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun ChatInputArea(
    onMessageSent: (String) -> Unit,
    isGenerating: Boolean
) {
    var messageText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text input field
        TextField(
            value = messageText,
            onValueChange = { messageText = it },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Type a message...") },
            maxLines = 3,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Send button
        Button(
            onClick = {
                if (messageText.isNotBlank()) {
                    onMessageSent(messageText)
                    messageText = ""
                }
            },
            enabled = messageText.isNotBlank() && !isGenerating,
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send message"
            )
        }
    }
}
