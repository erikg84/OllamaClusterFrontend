package usecase

import domain.model.*
import mu.KotlinLogging
import repository.NodeRepository

/**
 * Use case for retrieving all nodes in the cluster
 */
interface GetAllNodesUseCase {
    /**
     * Execute the use case to get all nodes
     * @return List of Node objects
     */
    suspend operator fun invoke(): List<Node>
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetAllNodesUseCase
 */
class GetAllNodesUseCaseImpl(private val nodeRepository: NodeRepository) : GetAllNodesUseCase {
    override suspend fun invoke(): List<Node> {
        logger.debug { "GetAllNodesUseCase: Fetching all nodes" }
        return nodeRepository.getAllNodes()
    }
}
