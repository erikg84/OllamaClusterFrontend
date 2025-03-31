package usecase

import domain.model.QueueStatus
import mu.KotlinLogging
import repository.QueueRepository

/**
 * Use case for retrieving the current status of the request queue
 */
interface GetQueueStatusUseCase {
    /**
     * Execute the use case to get the queue status
     * @return QueueStatus object with current queue information
     */
    suspend operator fun invoke(): QueueStatus
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetQueueStatusUseCase
 */
class GetQueueStatusUseCaseImpl(private val queueRepository: QueueRepository) : GetQueueStatusUseCase {
    override suspend fun invoke(): QueueStatus {
        logger.debug { "GetQueueStatusUseCase: Fetching queue status" }
        return queueRepository.getQueueStatus()
    }
}
