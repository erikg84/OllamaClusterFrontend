package ui.chatbot

import FileUploadDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.FrameWindowScope
import domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ui.MatrixThemeColors
import viewmodel.InteractViewModel

@Composable
fun ChatScreen(viewModel: InteractViewModel, composeWindow: ComposeWindow) {

    val chatMessages = viewModel.chatMessages
    val isGenerating by viewModel.isGenerating.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDashboardVisible by viewModel.isDashboardVisible.collectAsState()
    val clusterStatus by viewModel.clusterStatus.collectAsState()
    val activeNodes by viewModel.activeNodes.collectAsState()
    val queueSize by viewModel.queueSize.collectAsState()
    val responseTimes by viewModel.responseTimes.collectAsState()
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val memoryUsage by viewModel.memoryUsage.collectAsState()
    val gpuUsage by viewModel.gpuUsage.collectAsState()
    val messageAnimationStates = remember { mutableStateMapOf<String, Boolean>() }
    val isAnyMessageAnimating = messageAnimationStates.values.any { !it }
    var showFileUploadDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isAnyMessageAnimating) {
        if (isAnyMessageAnimating && chatMessages.isNotEmpty()) {
            while (isActive && isAnyMessageAnimating) {
                scrollState.animateScrollToItem(chatMessages.lastIndex)
                delay(100)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scrollToLatestEvent.collect {
            if (chatMessages.isNotEmpty()) {
                coroutineScope.launch {
                    scrollState.animateScrollToItem(chatMessages.size - 1)
                }
            }
        }
    }

    val logs = remember { mutableStateListOf<LogEntry>() }

    LaunchedEffect(Unit) {
        if (logs.isEmpty()) {
            logs.addAll(
                listOf(
                    LogEntry(
                        timestamp = "2025-04-08T14:30:22Z",
                        level = LogLevel.INFO,
                        message = "Node MAC_STUDIO connected to cluster",
                        source = "ClusterManager"
                    ),
                    LogEntry(
                        timestamp = "2025-04-08T14:30:45Z",
                        level = LogLevel.INFO,
                        message = "Model llama3.2:latest loaded successfully",
                        source = "ModelLoader"
                    ),
                    LogEntry(
                        timestamp = "2025-04-08T14:35:12Z",
                        level = LogLevel.WARN,
                        message = "High latency detected on BlackHP node",
                        source = "PerformanceMonitor"
                    ),
                    LogEntry(
                        timestamp = "2025-04-08T14:38:55Z",
                        level = LogLevel.ERROR,
                        message = "Request timeout on node BLACK_PRIME",
                        source = "RequestHandler"
                    ),
                    LogEntry(
                        timestamp = "2025-04-08T14:39:22Z",
                        level = LogLevel.DEBUG,
                        message = "Rebalancing load across 5 active nodes",
                        source = "LoadBalancer"
                    )
                )
            )
        }
    }

    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            // Initialize animation state for new messages
            chatMessages.forEachIndexed { index, message ->
                val key = "${message.role}_${message.content?.hashCode()}_$index"
                if (!messageAnimationStates.containsKey(key)) {
                    // Initialize: false = not completed, true = completed
                    val isCompleted = message.role != MessageRole.ASSISTANT ||
                            index < chatMessages.lastIndex
                    messageAnimationStates[key] = isCompleted
                }
            }

            // Scroll to the latest message
            scrollState.animateScrollToItem(chatMessages.lastIndex)
        }
    }

    // Refresh monitoring data at startup
    LaunchedEffect(Unit) {
        viewModel.refreshMonitoringData()
    }

    Box(modifier = Modifier.fillMaxSize().background(MatrixThemeColors.background)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Chat area (70% width)
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
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
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            itemsIndexed(chatMessages) { index, message ->
                                val key = "${message.role}_${message.content?.hashCode()}_$index"
                                val isAnimationComplete = messageAnimationStates[key] ?: true

                                if (!isAnimationComplete && message.role == MessageRole.ASSISTANT) {
                                    // Animated message (typing effect)
                                    AnimatedChatMessage(
                                        message = message,
                                        onCopyToClipboard = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Message copied to clipboard",
                                                    withDismissAction = true
                                                )
                                            }
                                        },
                                        onAnimationProgress = { progress ->
                                            // Trigger scroll on each animation update
                                            if (progress >= 1.0f) {
                                                messageAnimationStates[key] = true
                                            }

                                            // Ensure we're scrolled to the bottom during animation
                                            if (index == chatMessages.lastIndex) {
                                                coroutineScope.launch {
                                                    scrollState.animateScrollToItem(chatMessages.lastIndex)
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    // Static message (no animation)
                                    StaticChatMessage(
                                        message = message,
                                        onCopyToClipboard = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Message copied to clipboard",
                                                    withDismissAction = true
                                                )
                                            }
                                        }
                                    )
                                }
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
                        isGenerating = isGenerating,
                        onPlusButtonClicked = {
                            showFileUploadDialog = true
                        },
                    )
                }
            }

            // Monitoring dashboard (30% width)
            if (isDashboardVisible) {
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                        .background(MatrixThemeColors.background)
                        .padding(end = 8.dp)
                ) {
                    ImprovedMonitoringDashboard(
                        clusterStatus = clusterStatus,
                        activeNodes = activeNodes,
                        queueSize = queueSize,
                        responseTimes = listOf(TimeSeriesPoint(value = responseTimes)),
                        cpuUsage = cpuUsage,
                        memoryUsage = memoryUsage,
                        gpuUsage = gpuUsage,
                        logs = logs,
                        onRefresh = { viewModel.refreshMonitoringData() }
                    )
                }
            }
        }

        if (showFileUploadDialog) {
            FileUploadDialog(
                composeWindow = composeWindow,
                isVisible = showFileUploadDialog,
                onDismiss = { showFileUploadDialog = false },
                onFilesSelected = { files ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Uploaded ${files.size} files",
                            withDismissAction = true
                        )
                    }

                    if (files.isNotEmpty()) {
                        viewModel.saveFile(files.first())
                    }
                }
            )
        }

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
