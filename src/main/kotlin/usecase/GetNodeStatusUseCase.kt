package usecase

import domain.model.*
import mu.KotlinLogging
import repository.NodeRepository

/**
 * Use case for retrieving the status of all nodes
 */
interface GetNodeStatusUseCase {
    /**
     * Execute the use case to get node statuses
     * @return Map of node ID to NodeStatus
     */
    suspend operator fun invoke(): Map<String, NodeStatus>
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetNodeStatusUseCase
 */
class GetNodeStatusUseCaseImpl(private val nodeRepository: NodeRepository) : GetNodeStatusUseCase {
    override suspend fun invoke(): Map<String, NodeStatus> {
        logger.debug { "GetNodeStatusUseCase: Fetching node statuses" }
        return nodeRepository.getNodeStatus()
    }
}
