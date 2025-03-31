package usecase

import mu.KotlinLogging
import repository.QueueRepository

/**
 * Use case for resuming the request queue
 */
interface ResumeQueueUseCase {
    /**
     * Execute the use case to resume the queue
     * @return true if the operation was successful
     */
    suspend operator fun invoke(): Boolean
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of ResumeQueueUseCase
 */
class ResumeQueueUseCaseImpl(private val queueRepository: QueueRepository) : ResumeQueueUseCase {
    override suspend fun invoke(): Boolean {
        logger.debug { "ResumeQueueUseCase: Resuming queue" }
        val result = queueRepository.resumeQueue()
        logger.info { "Queue resume operation result: $result" }
        return result
    }
}
