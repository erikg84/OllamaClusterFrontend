package usecase

import mu.KotlinLogging
import repository.QueueRepository

/**
 * Use case for pausing the request queue
 */
interface PauseQueueUseCase {
    /**
     * Execute the use case to pause the queue
     * @return true if the operation was successful
     */
    suspend operator fun invoke(): Boolean
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of PauseQueueUseCase
 */
class PauseQueueUseCaseImpl(private val queueRepository: QueueRepository) : PauseQueueUseCase {
    override suspend fun invoke(): Boolean {
        logger.debug { "PauseQueueUseCase: Pausing queue" }
        val result = queueRepository.pauseQueue()
        logger.info { "Queue pause operation result: $result" }
        return result
    }
}
