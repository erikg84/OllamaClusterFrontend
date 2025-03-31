package usecase

import domain.model.*
import mu.KotlinLogging
import repository.ModelRepository

/**
 * Use case for retrieving models available on a specific node
 */
interface GetModelsByNodeUseCase {
    /**
     * Execute the use case to get models by node
     * @param nodeId The ID of the node
     * @return List of Model objects available on the specified node
     */
    suspend operator fun invoke(nodeId: String): List<Model>
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetModelsByNodeUseCase
 */
class GetModelsByNodeUseCaseImpl(private val modelRepository: ModelRepository) : GetModelsByNodeUseCase {
    override suspend fun invoke(nodeId: String): List<Model> {
        logger.debug { "GetModelsByNodeUseCase: Fetching models for node: $nodeId" }
        return modelRepository.getModelsByNode(nodeId)
    }
}
