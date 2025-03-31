package viewmodel

import domain.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import usecase.*
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

/**
 * ViewModel for the Dashboard screen
 */
class DashboardViewModel(
    private val getAllNodesUseCase: GetAllNodesUseCase,
    private val getAllModelsUseCase: GetAllModelsUseCase,
    private val getClusterStatusUseCase: GetClusterStatusUseCase,
    private val getQueueStatusUseCase: GetQueueStatusUseCase,
    private val pauseQueueUseCase: PauseQueueUseCase,
    private val resumeQueueUseCase: ResumeQueueUseCase
) : BaseViewModel() {

    // UI State
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    val nodes: StateFlow<List<Node>> = _nodes.asStateFlow()

    private val _models = MutableStateFlow<List<Model>>(emptyList())
    val models: StateFlow<List<Model>> = _models.asStateFlow()

    private val _clusterStatus = MutableStateFlow<ClusterStatus?>(null)
    val clusterStatus: StateFlow<ClusterStatus?> = _clusterStatus.asStateFlow()

    private val _queueStatus = MutableStateFlow<QueueStatus?>(null)
    val queueStatus: StateFlow<QueueStatus?> = _queueStatus.asStateFlow()

    private val _responseTimeData = MutableStateFlow<List<TimeSeriesPoint>>(emptyList())
    val responseTimeData: StateFlow<List<TimeSeriesPoint>> = _responseTimeData.asStateFlow()

    private val _pollingEnabled = MutableStateFlow(true)
    val pollingEnabled: StateFlow<Boolean> = _pollingEnabled.asStateFlow()

    private var pollingJob: Job? = null

    init {
        refresh()
        startPolling()
    }

    /**
     * Refresh all dashboard data
     */
    fun refresh() {
        logger.debug { "Refreshing dashboard data" }
        launchWithLoading {
            loadNodes()
            loadModels()
            loadClusterStatus()
            loadQueueStatus()
            updateResponseTimeData()
        }
    }

    /**
     * Start polling for updates
     */
    fun startPolling() {
        if (pollingJob != null) return

        _pollingEnabled.value = true
        pollingJob = viewModelScope.launch {
            while (_pollingEnabled.value) {
                delay(30.seconds)
                try {
                    loadNodes()
                    loadModels()
                    loadClusterStatus()
                    loadQueueStatus()
                } catch (e: Exception) {
                    logger.error(e) { "Error during polling" }
                }
            }
        }
    }

    /**
     * Stop polling for updates
     */
    fun stopPolling() {
        _pollingEnabled.value = false
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Toggle the queue status (pause/resume)
     */
    fun toggleQueueStatus() {
        launchWithLoading {
            val currentQueueStatus = _queueStatus.value
            if (currentQueueStatus != null) {
                val result = if (currentQueueStatus.active == true) {
                    logger.info { "Pausing queue" }
                    pauseQueueUseCase()
                } else {
                    logger.info { "Resuming queue" }
                    resumeQueueUseCase()
                }

                if (result) {
                    loadQueueStatus()
                }
            }
        }
    }

    private suspend fun loadNodes() {
        try {
            val result = getAllNodesUseCase()
            _nodes.value = result
            logger.debug { "Loaded ${result.size} nodes" }
        } catch (e: Exception) {
            logger.error(e) { "Error loading nodes" }
            handleError(e)
        }
    }

    private suspend fun loadModels() {
        try {
            val result = getAllModelsUseCase()
            _models.value = result
            logger.debug { "Loaded ${result.size} models" }
        } catch (e: Exception) {
            logger.error(e) { "Error loading models" }
            handleError(e)
        }
    }

    private suspend fun loadClusterStatus() {
        try {
            val result = getClusterStatusUseCase()
            _clusterStatus.value = result
            logger.debug { "Loaded cluster status" }
        } catch (e: Exception) {
            logger.error(e) { "Error loading cluster status" }
            handleError(e)
        }
    }

    private suspend fun loadQueueStatus() {
        try {
            val result = getQueueStatusUseCase()
            _queueStatus.value = result
            logger.debug { "Loaded queue status" }
        } catch (e: Exception) {
            logger.error(e) { "Error loading queue status" }
            handleError(e)
        }
    }

    // For demo purposes, generate sample response time data
    private fun updateResponseTimeData() {
        // In a real app, this would come from an API call
        val data = (0..12).map { index ->
            TimeSeriesPoint(
                time = "${index * 5}m",
                value = 200 + (Math.sin(index * 0.5) * 100)
            )
        }
        _responseTimeData.value = data
    }

    override fun clear() {
        stopPolling()
        super.clear()
    }
}
