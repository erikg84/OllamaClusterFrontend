package viewmodel

import domain.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mu.KotlinLogging
import usecase.*
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

class MetricsViewModel(
    private val getAllNodesUseCase: GetAllNodesUseCase,
    private val getAllModelsUseCase: GetAllModelsUseCase,
    private val getSystemMetricsUseCase: GetSystemMetricsUseCase
) : BaseViewModel() {

    // Writable state
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    val nodes: StateFlow<List<Node>> = _nodes.asStateFlow()

    private val _models = MutableStateFlow<List<Model>>(emptyList())
    val models: StateFlow<List<Model>> = _models.asStateFlow()

    private val _selectedNode = MutableStateFlow<String?>(null)
    val selectedNode: StateFlow<String?> = _selectedNode.asStateFlow()

    private val _selectedModel = MutableStateFlow<String?>(null)
    val selectedModel: StateFlow<String?> = _selectedModel.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.LAST_HOUR)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _metricsData = MutableStateFlow<MetricsData?>(null)
    val metricsData: StateFlow<MetricsData?> = _metricsData.asStateFlow()

    private val _responseTimeData = MutableStateFlow<List<TimeSeriesPoint>>(emptyList())
    val responseTimeData: StateFlow<List<TimeSeriesPoint>> = _responseTimeData.asStateFlow()

    private val _requestCountData = MutableStateFlow<List<TimeSeriesPoint>>(emptyList())
    val requestCountData: StateFlow<List<TimeSeriesPoint>> = _requestCountData.asStateFlow()

    private val _nodePerformance = MutableStateFlow<Map<String, NodePerformanceMetrics>>(emptyMap())
    val nodePerformance: StateFlow<Map<String, NodePerformanceMetrics>> = _nodePerformance.asStateFlow()

    private val _modelPerformance = MutableStateFlow<Map<String, ModelPerformanceMetrics>>(emptyMap())
    val modelPerformance: StateFlow<Map<String, ModelPerformanceMetrics>> = _modelPerformance.asStateFlow()

    private val _autoRefreshEnabled = MutableStateFlow(false)
    val autoRefreshEnabled: StateFlow<Boolean> = _autoRefreshEnabled.asStateFlow()

    private val _autoRefreshInterval = MutableStateFlow(30)
    val autoRefreshInterval: StateFlow<Int> = _autoRefreshInterval.asStateFlow()

    private var refreshJob: Job? = null

    init {
        loadNodesAndModels()
        loadMetrics()
    }

    fun loadNodesAndModels() {
        launchWithLoading {
            try {
                _nodes.value = getAllNodesUseCase()
                _models.value = getAllModelsUseCase()

                logger.debug { "Loaded ${_nodes.value.size} nodes and ${_models.value.size} models for filtering" }
            } catch (e: Exception) {
                logger.error(e) { "Error loading nodes and models" }
                handleError(e)
            }
        }
    }

    fun loadMetrics() {
        launchWithLoading {
            try {
                _metricsData.value = getSystemMetricsUseCase()
                processMetricsData()
                logger.debug { "Loaded metrics data" }
            } catch (e: Exception) {
                logger.error(e) { "Error loading metrics data" }
                handleError(e)
            }
        }
    }

    private fun processMetricsData() {
        _metricsData.value?.let { data ->
            _responseTimeData.value = _selectedNode.value?.let {
                data.responseTimes?.get(it)?.toList() ?: emptyList()
            } ?: data.responseTimes?.values?.flatten().orEmpty()

            _requestCountData.value = data.requestCounts.orEmpty()

            _nodePerformance.value = _selectedNode.value?.let {
                data.nodePerformance?.filterKeys { key -> key == it }
            } ?: data.nodePerformance.orEmpty()

            _modelPerformance.value = _selectedModel.value?.let {
                data.modelPerformance?.filterKeys { key -> key == it }
            } ?: data.modelPerformance.orEmpty()
        }
    }

    fun setNodeFilter(nodeId: String?) {
        _selectedNode.value = nodeId
        processMetricsData()
    }

    fun setModelFilter(modelId: String?) {
        _selectedModel.value = modelId
        processMetricsData()
    }

    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
        loadMetrics()
    }

    fun toggleAutoRefresh() {
        _autoRefreshEnabled.value = !_autoRefreshEnabled.value
        if (_autoRefreshEnabled.value) startAutoRefresh() else stopAutoRefresh()
    }

    fun setAutoRefreshInterval(seconds: Int) {
        if (seconds >= 5) {
            _autoRefreshInterval.value = seconds
            if (_autoRefreshEnabled.value) {
                stopAutoRefresh()
                startAutoRefresh()
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (_autoRefreshEnabled.value) {
                delay(_autoRefreshInterval.value.seconds)
                loadMetrics()
            }
        }
    }

    private fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    fun exportMetricsAsCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("time,metric,value")

        _responseTimeData.value.forEach {
            sb.appendLine("${it.time},response_time,${it.value}")
        }

        _requestCountData.value.forEach {
            sb.appendLine("${it.time},request_count,${it.value}")
        }

        return sb.toString()
    }

    override fun clear() {
        stopAutoRefresh()
        super.clear()
    }

    enum class TimeRange(val displayName: String) {
        LAST_HOUR("Last Hour"),
        LAST_DAY("Last Day"),
        LAST_WEEK("Last Week")
    }
}
