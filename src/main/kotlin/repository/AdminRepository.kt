package repository

import data.service.LLMApiService
import domain.model.*
import mu.KotlinLogging

/**
 * Repository interface for admin operations and monitoring
 */
interface AdminRepository {
    /**
     * Get performance metrics from the cluster
     * @return MetricsData object with response time, request counts, and performance data
     */
    suspend fun getMetrics(): MetricsData

    /**
     * Get system information about the cluster
     * @return SystemInfo object with API version, uptime, resource usage, etc.
     */
    suspend fun getSystemInfo(): SystemInfo

    /**
     * Reset statistics for the cluster
     * @return true if the operation was successful
     */
    suspend fun resetStats(): Boolean

    /**
     * Get system logs
     * @param level Optional log level filter
     * @return List of LogEntry objects
     */
    suspend fun getLogs(level: LogLevel? = null): List<LogEntry>

    /**
     * Get performance metrics
     * @return PerformanceMetrics with detailed model and node performance
     */
    suspend fun getPerformanceMetrics(): PerformanceMetrics

    /**
     * Get load balancing metrics
     * @return LoadBalancingMetrics with node load and routing information
     */
    suspend fun getLoadBalancingMetrics(): LoadBalancingMetrics

    /**
     * Get Prometheus metrics
     * @return Raw Prometheus metrics as a string
     */
    suspend fun getPrometheusMetrics(): String

    /**
     * Get cluster logs
     * @param level Optional log level filter
     * @return List of cluster-wide LogEntry objects
     */
    suspend fun getClusterLogs(level: LogLevel? = null): List<LogEntry>

    /**
     * Reset cluster-wide statistics
     * @return true if the operation was successful
     */
    suspend fun resetClusterStats(): Boolean

    /**
     * Get comprehensive cluster metrics
     * @return ClusterMetrics with detailed node and system information
     */
    suspend fun getClusterMetrics(): ClusterMetrics
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of AdminRepository that uses LLMApiService for admin operations
 */
class AdminRepositoryImpl(private val apiService: LLMApiService) : AdminRepository {

    override suspend fun getMetrics(): MetricsData {
        logger.debug { "Fetching cluster metrics" }
        return try {
            apiService.getMetrics()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching cluster metrics" }
            throw e
        }
    }

    override suspend fun getSystemInfo(): SystemInfo {
        logger.debug { "Fetching system information" }
        return try {
            apiService.getSystemInfo()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching system information" }
            throw e
        }
    }

    override suspend fun resetStats(): Boolean {
        logger.debug { "Resetting cluster statistics" }
        return try {
            val result = apiService.resetStats()
            logger.info { "Reset stats result: $result" }
            result
        } catch (e: Exception) {
            logger.error(e) { "Error resetting cluster statistics" }
            throw e
        }
    }

    override suspend fun getLogs(level: LogLevel?): List<LogEntry> {
        logger.debug { "Fetching system logs with level filter: $level" }
        return try {
            apiService.getLogs(level)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching system logs" }
            throw e
        }
    }

    override suspend fun getPerformanceMetrics(): PerformanceMetrics {
        logger.debug { "Fetching performance metrics" }
        return try {
            apiService.getPerformanceMetrics()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching performance metrics" }
            throw e
        }
    }

    override suspend fun getLoadBalancingMetrics(): LoadBalancingMetrics {
        logger.debug { "Fetching load balancing metrics" }
        return try {
            apiService.getLoadBalancingMetrics()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching load balancing metrics" }
            throw e
        }
    }

    override suspend fun getPrometheusMetrics(): String {
        logger.debug { "Fetching Prometheus metrics" }
        return try {
            apiService.getPrometheusMetrics()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching Prometheus metrics" }
            throw e
        }
    }

    override suspend fun getClusterLogs(level: LogLevel?): List<LogEntry> {
        logger.debug { "Fetching cluster logs with level filter: $level" }
        return try {
            apiService.getClusterLogs(level)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching cluster logs" }
            throw e
        }
    }

    override suspend fun resetClusterStats(): Boolean {
        logger.debug { "Resetting cluster statistics" }
        return try {
            val result = apiService.resetClusterStats()
            logger.info { "Reset cluster stats result: $result" }
            result
        } catch (e: Exception) {
            logger.error(e) { "Error resetting cluster statistics" }
            throw e
        }
    }

    override suspend fun getClusterMetrics(): ClusterMetrics {
        logger.debug { "Fetching cluster metrics" }
        return try {
            apiService.getClusterMetrics()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching cluster metrics" }
            throw e
        }
    }
}
