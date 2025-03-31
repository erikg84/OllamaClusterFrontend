package viewmodel

import androidx.compose.runtime.mutableStateListOf
import domain.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mu.KotlinLogging
import usecase.*
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

/**
 * ViewModel for the Admin screen
 */
class AdminViewModel(
    private val getAllNodesUseCase: GetAllNodesUseCase,
    private val getSystemInfoUseCase: GetSystemInfoUseCase,
    private val resetStatsUseCase: ResetStatsUseCase
) : BaseViewModel() {

    // Nodes for management
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    val nodes: StateFlow<List<Node>> = _nodes.asStateFlow()

    // System information
    private val _systemInfo = MutableStateFlow<SystemInfo?>(null)
    val systemInfo: StateFlow<SystemInfo?> = _systemInfo.asStateFlow()

    // Resource trends (CPU, Memory, Disk usage over time)
    val resourceTrends = mutableMapOf(
        "cpu" to mutableStateListOf<Double>(),
        "memory" to mutableStateListOf<Double>(),
        "disk" to mutableStateListOf<Double>()
    )

    // System logs
    val systemLogs = mutableStateListOf<LogEntry>()

    // Log filter
    private val _logLevelFilter = MutableStateFlow<LogLevel?>(null)
    val logLevelFilter: StateFlow<LogLevel?> = _logLevelFilter.asStateFlow()

    // Auto refresh
    private val _autoRefreshEnabled = MutableStateFlow(false)
    val autoRefreshEnabled: StateFlow<Boolean> = _autoRefreshEnabled.asStateFlow()

    private var refreshJob: Job? = null

    // System operation states
    private val _isPerformingOperation = MutableStateFlow(false)
    val isPerformingOperation: StateFlow<Boolean> = _isPerformingOperation.asStateFlow()

    private val _operationMessage = MutableStateFlow("")
    val operationMessage: StateFlow<String> = _operationMessage.asStateFlow()

    init {
        loadNodes()
        loadSystemInfo()
        // In a real implementation, we would also load logs here
        // But we'll simulate them for this demo
        simulateLogs()
    }

    /**
     * Load available nodes
     */
    fun loadNodes() {
        launchWithLoading {
            try {
                val nodesResult = getAllNodesUseCase()
                _nodes.value = nodesResult
                logger.debug { "Loaded ${nodesResult.size} nodes for admin" }
            } catch (e: Exception) {
                logger.error(e) { "Error loading nodes for admin" }
                handleError(e)
            }
        }
    }

    /**
     * Load system information
     */
    fun loadSystemInfo() {
        launchWithLoading {
            try {
                val systemInfoResult = getSystemInfoUseCase()
                _systemInfo.value = systemInfoResult

                // Update resource trends
                systemInfoResult.let { info ->
                    addResourceDataPoint("cpu", info.cpuUsage ?: 0.0)

                    // Parse memory usage for trend
                    val memUsed = parseUsage(info.memoryUsage?.used.orEmpty())
                    val memTotal = parseUsage(info.memoryUsage?.total.orEmpty())
                    if (memTotal > 0) {
                        addResourceDataPoint("memory", (memUsed / memTotal) * 100)
                    }

                    // Parse disk usage for trend
                    val diskUsed = parseUsage(info.diskUsage?.used.orEmpty())
                    val diskTotal = parseUsage(info.diskUsage?.total.orEmpty())
                    if (diskTotal > 0) {
                        addResourceDataPoint("disk", (diskUsed / diskTotal) * 100)
                    }
                }

                logger.debug { "Loaded system info" }
            } catch (e: Exception) {
                logger.error(e) { "Error loading system info" }
                handleError(e)
            }
        }
    }

    /**
     * Add a data point to resource trends
     */
    private fun addResourceDataPoint(resource: String, value: Double) {
        resourceTrends[resource]?.apply {
            // Keep only last 12 data points
            if (size >= 12) {
                removeAt(0)
            }
            add(value)
        }
    }

    /**
     * Parse usage string (e.g. "4.2 GB") to double (4.2)
     */
    private fun parseUsage(usageString: String): Double {
        val regex = "(\\d+(\\.\\d+)?)".toRegex()
        val match = regex.find(usageString)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    /**
     * Set log level filter
     */
    fun setLogLevelFilter(level: LogLevel?) {
        _logLevelFilter.value = level
        // In a real implementation, we would reload logs with this filter
    }

    /**
     * Simulate logs for demo
     */
    private fun simulateLogs() {
        // Add some sample logs
        systemLogs.add(
            LogEntry(
                timestamp = "2025-03-30 10:23:15",
                level = LogLevel.INFO,
                message = "Server started on port 3001",
                source = "Server"
            )
        )
        systemLogs.add(
            LogEntry(
                timestamp = "2025-03-30 10:23:16",
                level = LogLevel.INFO,
                message = "Connected to node: MAC_STUDIO",
                source = "NodeManager"
            )
        )
        systemLogs.add(
            LogEntry(
                timestamp = "2025-03-30 10:23:17",
                level = LogLevel.INFO,
                message = "Connected to node: MAC_MINI",
                source = "NodeManager"
            )
        )
        systemLogs.add(
            LogEntry(
                timestamp = "2025-03-30 10:23:18",
                level = LogLevel.ERROR,
                message = "Failed to connect to node: BLACK_PRIME",
                source = "NodeManager"
            )
        )
        systemLogs.add(
            LogEntry(
                timestamp = "2025-03-30 10:23:20",
                level = LogLevel.INFO,
                message = "Connected to node: SERVER_04",
                source = "NodeManager"
            )
        )
        systemLogs.add(
            LogEntry(
                timestamp = "2025-03-30 10:24:05",
                level = LogLevel.INFO,
                message = "Model loaded: llama3.2:latest on MAC_STUDIO",
                source = "ModelManager"
            )
        )
        systemLogs.add(
            LogEntry(
                timestamp = "2025-03-30 10:24:30",
                level = LogLevel.WARN,
                message = "High memory usage on MAC_MINI: 85%",
                source = "ResourceMonitor"
            )
        )
        systemLogs.add(
            LogEntry(
                timestamp = "2025-03-30 10:25:12",
                level = LogLevel.INFO,
                message = "Model loaded: mistral-7b:latest on MAC_MINI",
                source = "ModelManager"
            )
        )
    }

    /**
     * Toggle auto refresh
     */
    fun toggleAutoRefresh() {
        _autoRefreshEnabled.update { !it }

        if (_autoRefreshEnabled.value) {
            startAutoRefresh()
        } else {
            stopAutoRefresh()
        }
    }

    /**
     * Start auto refresh
     */
    private fun startAutoRefresh() {
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            while (_autoRefreshEnabled.value) {
                delay(10.seconds)
                loadSystemInfo()
                // In a real implementation, we would also reload logs
            }
        }
    }

    /**
     * Stop auto refresh
     */
    private fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    /**
     * Reset statistics
     */
    fun resetStats() {
        launchWithLoading {
            try {
                _isPerformingOperation.value = true
                setOperationMessage("Resetting statistics...")

                val result = resetStatsUseCase()

                if (result) {
                    setOperationMessage("Statistics reset successfully")
                } else {
                    setOperationMessage("Failed to reset statistics")
                }

                // Refresh data after operation
                loadSystemInfo()
            } catch (e: Exception) {
                logger.error(e) { "Error resetting statistics" }
                handleError(e)
                setOperationMessage("Error resetting statistics: ${e.message}")
            } finally {
                _isPerformingOperation.value = false
            }
        }
    }

    /**
     * Restart a node
     */
    fun restartNode(nodeId: String) {
        launchWithLoading {
            try {
                _isPerformingOperation.value = true
                setOperationMessage("Restarting node: $nodeId...")

                // Simulate node restart (in a real implementation, this would call the appropriate API)
                delay(2.seconds)

                setOperationMessage("Node $nodeId restarted successfully")

                // Refresh nodes after operation
                loadNodes()
            } catch (e: Exception) {
                logger.error(e) { "Error restarting node: $nodeId" }
                handleError(e)
                setOperationMessage("Error restarting node: ${e.message}")
            } finally {
                _isPerformingOperation.value = false
            }
        }
    }

    /**
     * Shutdown a node
     */
    fun shutdownNode(nodeId: String) {
        launchWithLoading {
            try {
                _isPerformingOperation.value = true
                setOperationMessage("Shutting down node: $nodeId...")

                // Simulate node shutdown (in a real implementation, this would call the appropriate API)
                delay(2.seconds)

                setOperationMessage("Node $nodeId shut down successfully")

                // Refresh nodes after operation
                loadNodes()
            } catch (e: Exception) {
                logger.error(e) { "Error shutting down node: $nodeId" }
                handleError(e)
                setOperationMessage("Error shutting down node: ${e.message}")
            } finally {
                _isPerformingOperation.value = false
            }
        }
    }

    /**
     * Update models on a node
     */
    fun updateModels(nodeId: String) {
        launchWithLoading {
            try {
                _isPerformingOperation.value = true
                setOperationMessage("Updating models on node: $nodeId...")

                // Simulate model update (in a real implementation, this would call the appropriate API)
                delay(3.seconds)

                setOperationMessage("Models on node $nodeId updated successfully")

                // Refresh nodes after operation
                loadNodes()
            } catch (e: Exception) {
                logger.error(e) { "Error updating models on node: $nodeId" }
                handleError(e)
                setOperationMessage("Error updating models: ${e.message}")
            } finally {
                _isPerformingOperation.value = false
            }
        }
    }

    /**
     * Purge the queue
     */
    fun purgeQueue() {
        launchWithLoading {
            try {
                _isPerformingOperation.value = true
                setOperationMessage("Purging queue...")

                // Simulate queue purge (in a real implementation, this would call the appropriate API)
                delay(1.seconds)

                setOperationMessage("Queue purged successfully")
            } catch (e: Exception) {
                logger.error(e) { "Error purging queue" }
                handleError(e)
                setOperationMessage("Error purging queue: ${e.message}")
            } finally {
                _isPerformingOperation.value = false
            }
        }
    }

    /**
     * Reset all connections
     */
    fun resetConnections() {
        launchWithLoading {
            try {
                _isPerformingOperation.value = true
                setOperationMessage("Resetting all connections...")

                // Simulate connection reset (in a real implementation, this would call the appropriate API)
                delay(2.seconds)

                setOperationMessage("All connections reset successfully")

                // Refresh nodes after operation
                loadNodes()
            } catch (e: Exception) {
                logger.error(e) { "Error resetting connections" }
                handleError(e)
                setOperationMessage("Error resetting connections: ${e.message}")
            } finally {
                _isPerformingOperation.value = false
            }
        }
    }

    /**
     * Clear system logs
     */
    fun clearLogs() {
        systemLogs.clear()
        setOperationMessage("Logs cleared")
    }

    /**
     * Export system logs as text
     */
    fun exportLogs(): String {
        val sb = StringBuilder()

        // Add header
        sb.appendLine("timestamp,level,source,message")

        // Add logs
        systemLogs.forEach { log ->
            sb.appendLine("${log.timestamp},${log.level},${log.source},${log.message}")
        }

        return sb.toString()
    }

    /**
     * Set operation message
     */
    private fun setOperationMessage(message: String) {
        logger.debug { "Operation: $message" }
        _operationMessage.value = message
    }

    override fun clear() {
        stopAutoRefresh()
        super.clear()
    }
}
