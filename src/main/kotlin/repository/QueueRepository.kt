package repository

import data.service.LLMApiService
import domain.model.*
import mu.KotlinLogging

/**
 * Repository interface for managing queue operations
 */
interface QueueRepository {
    /**
     * Get the current status of the request queue
     * @return QueueStatus object with active status and request counts
     */
    suspend fun getQueueStatus(): QueueStatus

    /**
     * Pause the request queue
     * @return true if the operation was successful
     */
    suspend fun pauseQueue(): Boolean

    /**
     * Resume the request queue
     * @return true if the operation was successful
     */
    suspend fun resumeQueue(): Boolean
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of QueueRepository that uses LLMApiService to manage queue operations
 */
class QueueRepositoryImpl(private val apiService: LLMApiService) : QueueRepository {

    override suspend fun getQueueStatus(): QueueStatus {
        logger.debug { "Fetching queue status" }
        return try {
            apiService.getQueueStatus()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching queue status" }
            throw e
        }
    }

    override suspend fun pauseQueue(): Boolean {
        logger.debug { "Pausing queue" }
        return try {
            val result = apiService.pauseQueue()
            logger.info { "Queue pause result: $result" }
            result
        } catch (e: Exception) {
            logger.error(e) { "Error pausing queue" }
            throw e
        }
    }

    override suspend fun resumeQueue(): Boolean {
        logger.debug { "Resuming queue" }
        return try {
            val result = apiService.resumeQueue()
            logger.info { "Queue resume result: $result" }
            result
        } catch (e: Exception) {
            logger.error(e) { "Error resuming queue" }
            throw e
        }
    }
}
