package repository

import data.service.LLMApiService
import domain.model.*
import mu.KotlinLogging

/**
 * Repository interface for managing cluster operations
 */
interface ClusterRepository {
    /**
     * Get the overall status of the cluster
     * @return ClusterStatus object with node and model information
     */
    suspend fun getClusterStatus(): ClusterStatus
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of ClusterRepository that uses LLMApiService to fetch cluster data
 */
class ClusterRepositoryImpl(private val apiService: LLMApiService) : ClusterRepository {

    override suspend fun getClusterStatus(): ClusterStatus {
        logger.debug { "Fetching cluster status" }
        return try {
            apiService.getClusterStatus()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching cluster status" }
            throw e
        }
    }
}
