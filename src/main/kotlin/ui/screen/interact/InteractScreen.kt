package ui.screen.interact

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    // Loading state
    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Error state
    val errors by viewModel.errors.collectAsState(initial = null)
    errors?.let { errorMessage ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    // Interact screen content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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

        Divider()

        // Chat or Prompt section based on the mode
        val interactionMode = viewModel.interactionMode.collectAsState().value
        when (interactionMode) {
            InteractViewModel.InteractionMode.CHAT -> {
                ChatSection(
                    messages = viewModel.chatMessages,
                    isGenerating = viewModel.isGenerating.collectAsState().value,
                    onMessageSent = { message -> viewModel.addUserMessage(message) },
                    onClearChat = { viewModel.clearChat() },
                    modifier = Modifier.weight(1f)
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
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Status message
        Text(
            text = viewModel.statusMessage.collectAsState().value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun InteractionModeSelector(
    currentMode: InteractViewModel.InteractionMode,
    streamResponses: Boolean,
    onModeSelected: (InteractViewModel.InteractionMode) -> Unit,
    onStreamToggled: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Mode selection row
        Text(
            text = "Interaction Mode",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chat mode button
            OutlinedButton(
                onClick = { onModeSelected(InteractViewModel.InteractionMode.CHAT) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (currentMode == InteractViewModel.InteractionMode.CHAT)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat Mode",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Chat")
            }

            // Generate mode button
            OutlinedButton(
                onClick = { onModeSelected(InteractViewModel.InteractionMode.GENERATE) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (currentMode == InteractViewModel.InteractionMode.GENERATE)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Generate Mode",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate")
            }
        }

        // Stream toggle row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Stream Responses",
                style = MaterialTheme.typography.bodyMedium
            )

            Switch(
                checked = streamResponses,
                onCheckedChange = { onStreamToggled() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Description text
        Text(
            text = if (streamResponses)
                "Responses will be shown as they are generated"
            else
                "Responses will be shown after completion",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
