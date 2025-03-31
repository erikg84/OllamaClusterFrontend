package usecase

import domain.model.*
import mu.KotlinLogging
import repository.NodeRepository

/**
 * Use case for retrieving a specific node by ID
 */
interface GetNodeByIdUseCase {
    /**
     * Execute the use case to get a node by ID
     * @param nodeId The ID of the node to retrieve
     * @return Node object if found
     */
    suspend operator fun invoke(nodeId: String): Node
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetNodeByIdUseCase
 */
class GetNodeByIdUseCaseImpl(private val nodeRepository: NodeRepository) : GetNodeByIdUseCase {
    override suspend fun invoke(nodeId: String): Node {
        logger.debug { "GetNodeByIdUseCase: Fetching node with ID: $nodeId" }
        return nodeRepository.getNodeById(nodeId)
    }
}
