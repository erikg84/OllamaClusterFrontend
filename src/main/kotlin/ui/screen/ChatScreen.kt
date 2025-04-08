package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.*
import domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import viewmodel.InteractViewModel
import kotlin.math.max


private object MatrixThemeColors {
    val background = Color(0xFF000000) // Pure black
    val surface = Color(0xFF0D0D0D) // Nearly black
    val cardBackground = Color(0xFF121212) // Slightly lighter than surface for cards
    val userMessageBg = Color(0xFF003B00) // Dark green
    val assistantMessageBg = Color(0xFF0F2318) // Darker green
    val userMessageText = Color(0xFF00FF00) // Bright matrix green
    val assistantMessageText = Color(0xFF4DFF4D) // Lighter matrix green
    val inputFieldBg = Color(0xFF0A1A0A) // Very dark green
    val buttonColor = Color(0xFF008F11) // Matrix code green
    val highlightColor = Color(0xFF00FF41) // Matrix highlight green
    val accentColor = Color(0xFF003B00) // Dark green accent
    val statusText = Color(0xFF4DFF4D) // Light green for status text
    val chartLine = Color(0xFF00FF41) // Bright green for charts
    val sectionHeader = Color(0xFF00FF41) // Bright green for section headers
    val separatorLine = Color(0xFF003B00) // Dark green for separators
    val errorColor = Color(0xFFFF5252) // Red for errors
    val infoColor = Color(0xFF4DFF4D) // Green for info
    val warningColor = Color(0xFFFFD740) // Yellow for warnings
    val debugColor = Color(0xFF40C4FF) // Blue for debug
}

@Composable
fun ChatScreen(viewModel: InteractViewModel) {

    val chatMessages = viewModel.chatMessages
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingEnabled by viewModel.streamResponses.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDashboardVisible by viewModel.isDashboardVisible.collectAsState()

    // Cluster monitoring data
    val clusterStatus by viewModel.clusterStatus.collectAsState()
    val activeNodes by viewModel.activeNodes.collectAsState()
    val queueSize by viewModel.queueSize.collectAsState()
    val responseTimes by viewModel.responseTimes.collectAsState()
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val memoryUsage by viewModel.memoryUsage.collectAsState()
    val gpuUsage by viewModel.gpuUsage.collectAsState()
    val performanceMetrics by viewModel.performanceMetrics.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()
    val messageAnimationStates = remember { mutableStateMapOf<String, Boolean>() }
    val messageDisplayStates = remember { mutableStateMapOf<String, Boolean>() }
    val isAnyMessageAnimating = messageAnimationStates.values.any { !it }

    LaunchedEffect(isAnyMessageAnimating) {
        if (isAnyMessageAnimating && chatMessages.isNotEmpty()) {
            // Continuously scroll to the bottom while animation is happening
            while (isActive && isAnyMessageAnimating) {
                scrollState.animateScrollToItem(chatMessages.lastIndex)
                delay(100) // Check scroll position frequently
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

    // Add state for logs (we'll fetch these in the viewModel)
    val logs = remember { mutableStateListOf<LogEntry>() }

    // For demonstration, add some sample logs if needed
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
                        isGenerating = isGenerating
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
fun ImprovedMonitoringDashboard(
    clusterStatus: ClusterStatus?,
    activeNodes: List<Node>,
    queueSize: Int,
    responseTimes: List<TimeSeriesPoint>,
    cpuUsage: Double,
    memoryUsage: Double,
    gpuUsage: Double,
    logs: List<LogEntry>,
    onRefresh: () -> Unit
) {
    val dashboardScroll = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp)
    ) {
        // Dashboard Header with Refresh Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYSTEM MONITOR",
                color = MatrixThemeColors.highlightColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh data",
                    tint = MatrixThemeColors.highlightColor
                )
            }
        }

        // Scrollable dashboard content
        LazyColumn(
            state = dashboardScroll,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cluster Status Card
            item {
                MonitoringCard(title = "Cluster Status") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Nodes online/total
                        BulletPoint(
                            text = "Nodes: ${activeNodes.size} online / ${clusterStatus?.totalNodes ?: "?"} total"
                        )

                        // Models loaded
                        BulletPoint(
                            text = "Models: ${clusterStatus?.loadedModels ?: "?"} loaded"
                        )

                        // Current queue
                        BulletPoint(
                            text = "Current queue: $queueSize requests"
                        )

                        // Display API version if available
                        if (clusterStatus != null) {
                            Divider(
                                color = MatrixThemeColors.separatorLine,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Text(
                                text = "LLM Cluster v3.2.1",
                                color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Active Nodes Card
            item {
                MonitoringCard(title = "Active Nodes") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeNodes.forEach { node ->
                            val nodeType = if (node.hardware?.gpu != null) "[GPU]" else "[CPU]"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BulletPoint(
                                    text = "${node.name ?: node.id} $nodeType",
                                    isActive = node.status == NodeStatus.ONLINE,
                                    modifier = Modifier.weight(1f)
                                )

                                // Show models loaded per node
                                Text(
                                    text = "${node.modelsLoaded ?: 0}",
                                    color = MatrixThemeColors.statusText,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (activeNodes.isEmpty()) {
                            Text(
                                text = "No active nodes",
                                color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Response Times Chart Card
            item {
                MonitoringCard(title = "Response Times (ms)") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                MatrixThemeColors.surface,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    ) {
                        if (responseTimes.isNotEmpty()) {
                            // Simple line chart implementation
                            ImprovedResponseTimesChart(responseTimes)
                        } else {
                            Text(
                                text = "No response time data available",
                                color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            // Resource Usage Card
            item {
                MonitoringCard(title = "Resource Usage") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // CPU Usage
                        ResourceUsageBar(
                            label = "CPU",
                            value = cpuUsage.toInt(),
                            maxValue = 100
                        )

                        // Memory Usage
                        ResourceUsageBar(
                            label = "MEM",
                            value = memoryUsage.toInt(),
                            maxValue = 100
                        )

                        // GPU Usage
                        ResourceUsageBar(
                            label = "GPU",
                            value = gpuUsage.toInt(),
                            maxValue = 100
                        )

                        // Queue
                        ResourceUsageBar(
                            label = "QUEUE",
                            value = queueSize,
                            maxValue = 10  // Assuming 10 is max queue size for display
                        )
                    }
                }
            }

            // System Logs Card
            item {
                MonitoringCard(title = "System Logs") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        logs.takeLast(5).forEach { log ->
                            LogEntryRow(log)
                        }

                        if (logs.isEmpty()) {
                            Text(
                                text = "No logs available",
                                color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Performance Stats Card
            item {
                MonitoringCard(title = "Performance Stats") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DataRow(
                            label = "Avg. Response",
                            value = "526 ms"
                        )

                        DataRow(
                            label = "Tokens/Sec",
                            value = "145"
                        )

                        DataRow(
                            label = "Requests/Min",
                            value = "32"
                        )

                        DataRow(
                            label = "Error Rate",
                            value = "0.5%"
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MonitoringCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MatrixThemeColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Section header
            Text(
                text = title,
                color = MatrixThemeColors.sectionHeader,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth()
            )

            // Separator line
            Divider(
                color = MatrixThemeColors.separatorLine,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Section content
            content()
        }
    }
}

@Composable
fun LogEntryRow(log: LogEntry) {
    val logColor = when (log.level) {
        LogLevel.ERROR -> MatrixThemeColors.errorColor
        LogLevel.WARN -> MatrixThemeColors.warningColor
        LogLevel.INFO -> MatrixThemeColors.infoColor
        LogLevel.DEBUG -> MatrixThemeColors.debugColor
        else -> MatrixThemeColors.statusText
    }

    val timestamp = log.timestamp?.substringAfter("T")?.substringBefore("Z") ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Log level indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(logColor, shape = RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Log content
        Column {
            // Time and source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timestamp,
                    color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = log.source ?: "",
                    color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Log message
            Text(
                text = log.message ?: "",
                color = logColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MatrixThemeColors.statusText,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = value,
            color = MatrixThemeColors.highlightColor,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

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

@Composable
fun ImprovedResponseTimesChart(responseTimes: List<TimeSeriesPoint>) {
    // Find min and max values
    val values = responseTimes.mapNotNull { it.value }
    val maxValue = values.maxOrNull() ?: 100.0
    val minValue = values.minOrNull() ?: 0.0
    val range = max(1.0, maxValue - minValue)

    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val pointWidth = width / (responseTimes.size - 1).coerceAtLeast(1)

            // Draw axis labels using the proper drawText API
            drawText(
                textMeasurer = textMeasurer,
                text = "${maxValue.toInt()}",
                topLeft = Offset(0f, 10f),
                style = TextStyle(
                    color = MatrixThemeColors.statusText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "${minValue.toInt()}",
                topLeft = Offset(0f, height - 15f),
                style = TextStyle(
                    color = MatrixThemeColors.statusText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            )

            // Draw grid lines
            val gridColor = MatrixThemeColors.separatorLine.copy(alpha = 0.3f)
            val gridSteps = 3
            for (i in 0..gridSteps) {
                val y = height * i / gridSteps
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 0.5f
                )
            }

            // Draw points and lines
            if (responseTimes.size > 1) {
                for (i in 0 until responseTimes.size - 1) {
                    val currentValue = responseTimes[i].value ?: continue
                    val nextValue = responseTimes[i + 1].value ?: continue

                    val x1 = i * pointWidth
                    val y1 = height - ((currentValue - minValue) / range * height).toFloat()
                    val x2 = (i + 1) * pointWidth
                    val y2 = height - ((nextValue - minValue) / range * height).toFloat()

                    // Draw line between points
                    drawLine(
                        color = MatrixThemeColors.chartLine,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2f
                    )

                    // Draw point
                    drawCircle(
                        color = MatrixThemeColors.chartLine,
                        radius = 3f,
                        center = Offset(x1, y1)
                    )
                }

                // Draw last point
                val lastIndex = responseTimes.size - 1
                val lastValue = responseTimes[lastIndex].value ?: return@Canvas
                val x = lastIndex * pointWidth
                val y = height - ((lastValue - minValue) / range * height).toFloat()

                drawCircle(
                    color = MatrixThemeColors.chartLine,
                    radius = 3f,
                    center = Offset(x, y)
                )
            } else if (responseTimes.size == 1) {
                // Draw single point
                val value = responseTimes[0].value ?: return@Canvas
                val y = height - ((value - minValue) / range * height).toFloat()

                drawCircle(
                    color = MatrixThemeColors.chartLine,
                    radius = 3f,
                    center = Offset(width / 2, y)
                )
            }
        }
    }
}

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

