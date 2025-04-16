package viewmodel

import androidx.compose.runtime.mutableStateListOf
import domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import repository.AdminRepository
import repository.ClusterRepository
import repository.NodeRepository
import repository.QueueRepository
import usecase.*
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * ViewModel for the LLM Interaction screen
 */
class InteractViewModel(
    private val getAllNodesUseCase: GetAllNodesUseCase,
    private val getAllModelsUseCase: GetAllModelsUseCase,
    private val chatWithLLMUseCase: ChatWithLLMUseCase,
    private val generateTextUseCase: GenerateTextUseCase,
    private val clusterRepository: ClusterRepository,
    private val adminRepository: AdminRepository,
    private val queueRepository: QueueRepository,
    private val nodeRepository: NodeRepository,
    private val sendVisionRequestUseCase: SendVisionRequestUseCase
) : BaseViewModel() {

    // Available nodes and models
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    val nodes: StateFlow<List<Node>> = _nodes.asStateFlow()

    private val _models = MutableStateFlow<List<Model>>(emptyList())
    val models: StateFlow<List<Model>> = _models.asStateFlow()

    // Selected options
    private val _selectedNode = MutableStateFlow<Node?>(Node(id = "MAC_STUDIO", name = "MAC_STUDIO"))
    val selectedNode: StateFlow<Node?> = _selectedNode.asStateFlow()

    private val _selectedModel = MutableStateFlow<Model?>(Model(id = "llama3.2:latest", name = "llama3.2:latest"))
    val selectedModel: StateFlow<Model?> = _selectedModel.asStateFlow()

    private val _interactionMode = MutableStateFlow(InteractionMode.CHAT)
    val interactionMode: StateFlow<InteractionMode> = _interactionMode.asStateFlow()

    private val _streamResponses = MutableStateFlow(false)
    val streamResponses: StateFlow<Boolean> = _streamResponses.asStateFlow()

    // Chat history
    val chatMessages = mutableStateListOf<ChatMessage>()

    // Generate mode properties
    private val _prompt = MutableStateFlow("")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _generatedText = MutableStateFlow("")
    val generatedText: StateFlow<String> = _generatedText.asStateFlow()

    // Parameters
    private val _temperature = MutableStateFlow(0.7)
    val temperature: StateFlow<Double> = _temperature.asStateFlow()

    private val _topP = MutableStateFlow(0.9)
    val topP: StateFlow<Double> = _topP.asStateFlow()

    private val _maxTokens = MutableStateFlow(1024)
    val maxTokens: StateFlow<Int> = _maxTokens.asStateFlow()

    private val _frequencyPenalty = MutableStateFlow(0.0)
    val frequencyPenalty: StateFlow<Double> = _frequencyPenalty.asStateFlow()

    // Status message
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // Is generating a response
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _clusterStatus = MutableStateFlow<ClusterStatus?>(null)
    val clusterStatus: StateFlow<ClusterStatus?> = _clusterStatus.asStateFlow()

    private val _activeNodes = MutableStateFlow<List<Node>>(emptyList())
    val activeNodes: StateFlow<List<Node>> = _activeNodes.asStateFlow()

    private val _queueStatus = MutableStateFlow<QueueStatus?>(null)
    val queueStatus: StateFlow<QueueStatus?> = _queueStatus.asStateFlow()

    private val _performanceMetrics = MutableStateFlow<PerformanceMetrics?>(null)
    val performanceMetrics: StateFlow<PerformanceMetrics?> = _performanceMetrics.asStateFlow()

    private val _systemInfo = MutableStateFlow<SystemInfo?>(null)
    val systemInfo: StateFlow<SystemInfo?> = _systemInfo.asStateFlow()

    // For response times chart
    private val _responseTimes = MutableStateFlow(0.0)
    val responseTimes: StateFlow<Double> = _responseTimes.asStateFlow()

    // For resource usage
    private val _resourceUsage = MutableStateFlow<Map<String, Double>>(emptyMap())
    val resourceUsage: StateFlow<Map<String, Double>> = _resourceUsage.asStateFlow()

    // UI state for layout
    private val _isDashboardVisible = MutableStateFlow(true)
    val isDashboardVisible: StateFlow<Boolean> = _isDashboardVisible.asStateFlow()

    private val _cpuUsage = MutableStateFlow(0.0)
    val cpuUsage: StateFlow<Double> = _cpuUsage.asStateFlow()

    private val _memoryUsage = MutableStateFlow(0.0)
    val memoryUsage: StateFlow<Double> = _memoryUsage.asStateFlow()

    private val _gpuUsage = MutableStateFlow(0.0)
    val gpuUsage: StateFlow<Double> = _gpuUsage.asStateFlow()

    private val _queueSize = MutableStateFlow(0)
    val queueSize: StateFlow<Int> = _queueSize.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _scrollToLatestEvent = MutableSharedFlow<Unit>()
    val scrollToLatestEvent = _scrollToLatestEvent.asSharedFlow()

    private val _visionResult = MutableStateFlow("")
    val visionResult: StateFlow<String> = _visionResult.asStateFlow()

    private var file: File? = null


    // Timer for auto-refresh
    private var monitoringJob: Job? = null
    private val monitoringInterval = 5000L

    init {
        loadNodesAndModels()
        startMonitoring()
        refreshLogs()
    }

    fun refreshLogs() {
        viewModelScope.launch {
            try {
                val latestLogs = adminRepository.getLogs()
                _logs.value = latestLogs.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                handleError(e)
                setStatusMessage("Error fetching logs: ${e.message}")
            }
        }
    }

    /**
     * Load available nodes and models
     */
    fun loadNodesAndModels() {
        launchWithLoading {
            try {
                val allNodes = getAllNodesUseCase()
                _nodes.value = allNodes.filter { it.status == NodeStatus.ONLINE }

                val allModels = getAllModelsUseCase()
                _models.value = allModels.filter { it.status == ModelStatus.LOADED }

                // Auto-select first node and model if available
                if (_selectedNode.value == null && _nodes.value.isNotEmpty()) {
                    selectNode(_nodes.value.first())
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun sendVisionRequest(content: String) {
        if (file == null) return
        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = content
        )
        chatMessages.add(userMessage)

        val modelId = _selectedModel.value?.id
        val nodeId = _selectedNode.value?.id

        if (modelId.isNullOrBlank() || nodeId.isNullOrBlank()) {
            setStatusMessage("Please select a model and node before making a vision request.")
            return
        }

        launchWithLoading {
            try {
                _isGenerating.value = true
                val result = sendVisionRequestUseCase(VisionRequest(modelId, content, nodeId, file!!))
                _visionResult.value = result.message?.content.orEmpty()
                setStatusMessage("Vision request completed")
                file = null
            } catch (e: Exception) {
                handleError(e)
                setStatusMessage("Vision request failed: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun saveFile(file: File) {
        this.file = file
    }


    /**
     * Select a node
     */
    fun selectNode(node: Node) {
        _selectedNode.value = node
        // Filter models for this node
        val nodeModels = _models.value.filter { it.node == node.id }
        // Auto-select first model for this node if available
        if (nodeModels.isNotEmpty()) {
            selectModel(nodeModels.first())
        } else {
            _selectedModel.value = null
        }
    }

    /**
     * Select a model
     */
    fun selectModel(model: Model) {
        _selectedModel.value = model
    }

    /**
     * Set interaction mode (chat or generate)
     */
    fun setInteractionMode(mode: InteractionMode) {
        _interactionMode.value = mode
    }

    /**
     * Toggle streaming responses
     */
    fun toggleStreamResponses() {
        _streamResponses.value = !_streamResponses.value
    }

    /**
     * Set prompt for generation
     */
    fun setPrompt(newPrompt: String) {
        _prompt.value = newPrompt
    }

    /**
     * Clear generated text
     */
    fun clearGeneratedText() {
        _generatedText.value = ""
    }

    fun scrollToLatestMessage() {
        viewModelScope.launch {
            if (chatMessages.isNotEmpty()) {
                // Signal UI to scroll to latest message
                // (We'll add an event flow for this)
                _scrollToLatestEvent.emit(Unit)
            }
        }
    }

    /**
     * Add a user message to chat
     */
    fun addUserMessage(content: String) {
        if (content.isBlank()) return

        if (file != null) {
            sendVisionRequest(content)
            return
        }

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = content
        )
        chatMessages.add(userMessage)

        // If in chat mode, send message to LLM
        if (_interactionMode.value == InteractionMode.CHAT) {
            sendChatRequest()
        }
    }

    /**
     * Clear chat history
     */
    fun clearChat() {
        chatMessages.clear()
    }

    /**
     * Update LLM parameters
     */
    fun updateParameters(
        newTemperature: Double? = null,
        newTopP: Double? = null,
        newMaxTokens: Int? = null,
        newFrequencyPenalty: Double? = null
    ) {
        newTemperature?.let { _temperature.value = it }
        newTopP?.let { _topP.value = it }
        newMaxTokens?.let { _maxTokens.value = it }
        newFrequencyPenalty?.let { _frequencyPenalty.value = it }
    }

    /**
     * Send a prompt to generate text
     */
    fun generateText() {
        if (_prompt.value.isBlank() || _selectedNode.value == null || _selectedModel.value == null) {
            setStatusMessage("Please select a node, model, and enter a prompt")
            return
        }

        val request = GenerateRequest(
            node = _selectedNode.value!!.id,
            model = _selectedModel.value!!.id,
            prompt = _prompt.value,
            stream = _streamResponses.value
        )

        launchWithLoading {
            try {
                _isGenerating.value = true

                if (_streamResponses.value) {
                    // Handle streaming response
                    val responseFlow = generateTextUseCase.stream(request)
                    processGenerateResponseStream(responseFlow)
                } else {
                    // Handle non-streaming response
                    val response = generateTextUseCase(request) as GenerateResponse
                    _generatedText.value = response.text.orEmpty()
                    setStatusMessage("Text generated successfully")
                }
            } catch (e: Exception) {
                handleError(e)
                setStatusMessage("Error generating text: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Send a chat request to the LLM
     */
    private fun sendChatRequestNoStream() {
        if (_selectedNode.value == null || _selectedModel.value == null || chatMessages.isEmpty()) {
            setStatusMessage("Please select a node, model, and enter a message")
            return
        }

        val request = ChatRequest(
            node = _selectedNode.value!!.id,
            model = _selectedModel.value!!.id,
            messages = chatMessages.toList(),
            stream = _streamResponses.value,
        )

        viewModelScope.launch {
            try {
                _isGenerating.value = true

                if (_streamResponses.value) {
                    // Handle streaming response
                    val responseFlow = chatWithLLMUseCase.stream(request)
                    processChatResponseStream(responseFlow)
                } else {
                    // Handle non-streaming response
                    val response = chatWithLLMUseCase(request) as ChatResponse
                    response.message?.let { chatMessages.add(it) }
                    setStatusMessage("Response received")
                }
            } catch (e: Exception) {
                handleError(e)
                setStatusMessage("Error in chat: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Process a streaming chat response
     */
    private suspend fun processChatResponseStream(responseFlow: Flow<ChatResponse>) {
        // Add a placeholder for the streaming response
        val assistantMessage = ChatMessage(
            role = MessageRole.ASSISTANT,
            content = ""
        )
        chatMessages.add(assistantMessage)

        var streamedContent = ""

        responseFlow
            .onStart { setStatusMessage("Streaming response...") }
            .onEach { response ->
                // Update the content with the streamed chunk
                streamedContent = response.message?.content.orEmpty()
                val updatedMessage = assistantMessage.copy(content = streamedContent)
                chatMessages[chatMessages.lastIndex] = updatedMessage
            }
            .onCompletion {
                setStatusMessage("Response completed")
            }
            .catch { e ->
                handleError(e)
                setStatusMessage("Error in streaming: ${e.message}")
            }
            .collect()
    }

    /**
     * Process a streaming generate response
     */
    private suspend fun processGenerateResponseStream(responseFlow: Flow<GenerateResponse>) {
        var streamedText = ""

        responseFlow
            .onStart {
                setStatusMessage("Streaming text...")
                _generatedText.value = ""
            }
            .onEach { response ->
                // Append the streamed chunk
                streamedText += response.text
                _generatedText.value = streamedText
            }
            .onCompletion {
                setStatusMessage("Generation completed")
            }
            .catch { e ->
                handleError(e)
                setStatusMessage("Error in streaming: ${e.message}")
            }
            .collect()
    }

    /**
     * Set a status message
     */
    private fun setStatusMessage(message: String) {
        logger.debug { "Status: $message" }
        _statusMessage.value = message
    }

    /**
     * Interaction modes
     */
    enum class InteractionMode {
        CHAT, GENERATE
    }

    /**
     * Toggle dashboard visibility
     */
    fun toggleDashboard() {
        _isDashboardVisible.value = !_isDashboardVisible.value
    }

    /**
     * Start periodic monitoring of cluster stats
     */
    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            while (isActive) {
                refreshMonitoringData()
                delay(monitoringInterval)
            }
        }
    }

    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    /**
     * Refresh all monitoring data
     */
    fun refreshMonitoringData() {
        viewModelScope.launch {
            try {
                // Fetch all monitoring data in parallel
                val clusterStatusDeferred = async { clusterRepository.getClusterStatus() }
                val queueStatusDeferred = async { queueRepository.getQueueStatus() }
                val metricsDeferred = async { adminRepository.getPerformanceMetrics() }
                val systemInfoDeferred = async { adminRepository.getSystemInfo() }
                val nodesDeferred = async { nodeRepository.getAllNodes() }
                val logsDeferred = async { adminRepository.getLogs() }

                // Get all active nodes
                val allNodes = nodesDeferred.await()
                _activeNodes.value = allNodes.filter { it.status == NodeStatus.ONLINE }

                // Update cluster status
                _clusterStatus.value = clusterStatusDeferred.await()

                // Update queue status
                val queueStatus = queueStatusDeferred.await()
                _queueStatus.value = queueStatus
                _queueSize.value = queueStatus.pending ?: 0

                // Update performance metrics and extract response times
                val metrics = metricsDeferred.await()
                _performanceMetrics.value = metrics

                // Get system info for resource usage
                val sysInfo = systemInfoDeferred.await()
                _systemInfo.value = sysInfo

                val latestLogs = logsDeferred.await()
                _logs.value = latestLogs.sortedByDescending { it.timestamp }

                // Set CPU usage directly
                _cpuUsage.value = sysInfo.cpuUsage ?: 0.0

                // Calculate memory usage percentage
                sysInfo.memoryUsage?.let {
                    val memUsed = it.used?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0
                    val memTotal = it.total?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 1.0
                    if (memTotal > 0) {
                        _memoryUsage.value = (memUsed / memTotal) * 100.0
                    }
                }

                // Calculate GPU usage based on node metrics if available
                // Otherwise estimate based on queue and active nodes
                val nodeMetrics = metrics.nodeMetrics
                if (nodeMetrics != null && nodeMetrics.isNotEmpty()) {
                    // Average GPU usage across all nodes
                    val avgGpuUsage = nodeMetrics.values
                        .mapNotNull { it.requestCount.toDouble() / 100.0 }
                        .average()
                        .coerceIn(0.0, 100.0)
                    _gpuUsage.value = avgGpuUsage * 100.0 // Scale to percentage
                } else {
                    // Fallback estimation
                    val queueSizeValue = _queueSize.value
                    val activeNodeCount = _activeNodes.value.size.coerceAtLeast(1)
                    _gpuUsage.value = (queueSizeValue.toDouble() / activeNodeCount).coerceIn(0.0, 100.0) * 70.0
                }

                metrics.nodeMetrics.values.firstOrNull()?.avgResponseTime?.let {
                    _responseTimes.value = it
                }

                setStatusMessage("Monitoring data refreshed")
            } catch (e: Exception) {
                handleError(e)
                setStatusMessage("Error refreshing monitoring data: ${e.message}")
            }
        }
    }

    private fun sendChatRequest() {
        if (_selectedNode.value == null || _selectedModel.value == null || chatMessages.isEmpty()) {
            setStatusMessage("Please select a node, model, and enter a message")
            return
        }

        val request = ChatRequest(
            node = _selectedNode.value!!.id,
            model = _selectedModel.value!!.id,
            messages = chatMessages.toList(),
            stream = _streamResponses.value,
        )

        viewModelScope.launch {
            try {
                _isGenerating.value = true

                if (_streamResponses.value) {
                    // Create a placeholder message for streaming
                    val assistantMessage = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = ""
                    )
                    chatMessages.add(assistantMessage)

                    // Handle streaming response
                    val responseFlow = chatWithLLMUseCase.stream(request)
                    processChatResponseStream(responseFlow, assistantMessage)
                    scrollToLatestMessage()
                } else {
                    // Handle non-streaming response
                    val response = chatWithLLMUseCase(request) as ChatResponse
                    response.message?.let { chatMessages.add(it) }
                    setStatusMessage("Response received")
                    scrollToLatestMessage()
                }
            } catch (e: Exception) {
                handleError(e)
                setStatusMessage("Error in chat: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // Update the processChatResponseStream function to update a specific message
    private suspend fun processChatResponseStream(
        responseFlow: Flow<ChatResponse>,
        assistantMessage: ChatMessage
    ) {
        var streamedContent = ""

        responseFlow
            .onStart { setStatusMessage("Streaming response...") }
            .onEach { response ->
                // Update the content with the streamed chunk
                val newContent = response.message?.content ?: ""
                if (newContent.isNotEmpty()) {
                    streamedContent += newContent
                    val updatedMessage = assistantMessage.copy(content = streamedContent)

                    // Find the index of the placeholder message and update it
                    val index = chatMessages.indexOf(assistantMessage)
                    if (index >= 0) {
                        chatMessages[index] = updatedMessage
                    }
                }
            }
            .onCompletion {
                setStatusMessage("Response completed")
            }
            .catch { e ->
                handleError(e)
                setStatusMessage("Error in streaming: ${e.message}")
            }
            .collect()
    }

    override fun clear() {
        super.clear()
        stopMonitoring()
    }
}
