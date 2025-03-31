package viewmodel

import androidx.compose.runtime.mutableStateListOf
import domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import mu.KotlinLogging
import usecase.*

private val logger = KotlinLogging.logger {}

/**
 * ViewModel for the LLM Interaction screen
 */
class InteractViewModel(
    private val getAllNodesUseCase: GetAllNodesUseCase,
    private val getAllModelsUseCase: GetAllModelsUseCase,
    private val chatWithLLMUseCase: ChatWithLLMUseCase,
    private val generateTextUseCase: GenerateTextUseCase
) : BaseViewModel() {

    // Available nodes and models
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    val nodes: StateFlow<List<Node>> = _nodes.asStateFlow()

    private val _models = MutableStateFlow<List<Model>>(emptyList())
    val models: StateFlow<List<Model>> = _models.asStateFlow()

    // Selected options
    private val _selectedNode = MutableStateFlow<Node?>(null)
    val selectedNode: StateFlow<Node?> = _selectedNode.asStateFlow()

    private val _selectedModel = MutableStateFlow<Model?>(null)
    val selectedModel: StateFlow<Model?> = _selectedModel.asStateFlow()

    private val _interactionMode = MutableStateFlow(InteractionMode.CHAT)
    val interactionMode: StateFlow<InteractionMode> = _interactionMode.asStateFlow()

    private val _streamResponses = MutableStateFlow(true)
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

    init {
        loadNodesAndModels()
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

    /**
     * Add a user message to chat
     */
    fun addUserMessage(content: String) {
        if (content.isBlank()) return

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
            stream = _streamResponses.value,
            parameters = LLMParameters(
                temperature = _temperature.value,
                topP = _topP.value,
                maxTokens = _maxTokens.value,
                frequencyPenalty = _frequencyPenalty.value
            )
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
            parameters = LLMParameters(
                temperature = _temperature.value,
                topP = _topP.value,
                maxTokens = _maxTokens.value,
                frequencyPenalty = _frequencyPenalty.value
            )
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
}
