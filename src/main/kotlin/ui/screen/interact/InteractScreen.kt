package ui.screen.interact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodel.InteractViewModel

@Composable
fun InteractScreen(viewModel: InteractViewModel) {
    // Load nodes and models when the screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadNodesAndModels()
    }

    // State for error dialog
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Collect error state
    val errors by viewModel.errors.collectAsState(initial = null)

    // Show error dialog when errors are present
    LaunchedEffect(errors) {
        errors?.let {
            errorMessage = it
            showErrorDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Split screen layout
        Row(modifier = Modifier.fillMaxSize()) {
            // Left side - Chat (60% width)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
                    .padding(16.dp)
            ) {
                val interactionMode = viewModel.interactionMode.collectAsState().value

                when (interactionMode) {
                    InteractViewModel.InteractionMode.CHAT -> {
                        ChatSection(
                            messages = viewModel.chatMessages,
                            isGenerating = viewModel.isGenerating.collectAsState().value,
                            onMessageSent = { message -> viewModel.addUserMessage(message) },
                            onClearChat = { viewModel.clearChat() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    InteractViewModel.InteractionMode.GENERATE -> {
                        PromptSection(
                            prompt = viewModel.prompt.collectAsState().value,
                            generatedText = viewModel.generatedText.collectAsState().value,
                            isGenerating = viewModel.isGenerating.collectAsState().value,
                            onPromptChanged = { viewModel.setPrompt(it) },
                            onGenerateClicked = { viewModel.generateText() },
                            onClearOutput = { viewModel.clearGeneratedText() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Loading overlay for the chat section only
                if (viewModel.isGenerating.collectAsState().value) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            // Right side - Settings (40% width)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
                    .padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                // Scrollable container for all settings
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "LLM Interaction",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Node and model selection
                    val selectedNode = viewModel.selectedNode.collectAsState().value
                    NodeModelSelector(
                        nodes = viewModel.nodes.collectAsState().value,
                        models = viewModel.models.collectAsState().value.filter {
                            selectedNode?.id == it.node
                        },
                        selectedNode = selectedNode,
                        selectedModel = viewModel.selectedModel.collectAsState().value,
                        onNodeSelected = { viewModel.selectNode(it) },
                        onModelSelected = { viewModel.selectModel(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Mode selector (Chat vs Generate) and stream toggle
                    InteractionModeSelector(
                        currentMode = viewModel.interactionMode.collectAsState().value,
                        streamResponses = viewModel.streamResponses.collectAsState().value,
                        onModeSelected = { viewModel.setInteractionMode(it) },
                        onStreamToggled = { viewModel.toggleStreamResponses() }
                    )

                    // Parameters section
                    ParametersSection(
                        temperature = viewModel.temperature.collectAsState().value,
                        topP = viewModel.topP.collectAsState().value,
                        maxTokens = viewModel.maxTokens.collectAsState().value,
                        frequencyPenalty = viewModel.frequencyPenalty.collectAsState().value,
                        onParametersChanged = { temp, tp, mt, fp ->
                            viewModel.updateParameters(temp, tp, mt, fp)
                        }
                    )

                    // Status message
                    Text(
                        text = viewModel.statusMessage.collectAsState().value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // Clear button
                    val interactionMode = viewModel.interactionMode.collectAsState().value
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            onClick = {
                                if (interactionMode == InteractViewModel.InteractionMode.CHAT) {
                                    viewModel.clearChat()
                                } else {
                                    viewModel.clearGeneratedText()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Clear")
                        }
                    }

                    // Add some padding at the bottom for better scrolling
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Global loading overlay - only for initial loading, not for message generation
        if (viewModel.isLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text("Error")
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}