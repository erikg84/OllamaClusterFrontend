package usecase

import domain.model.*
import mu.KotlinLogging
import repository.ModelRepository

/**
 * Use case for retrieving a specific model by ID
 */
interface GetModelByIdUseCase {
    /**
     * Execute the use case to get a model by ID
     * @param modelId The ID of the model to retrieve
     * @return Model object if found
     */
    suspend operator fun invoke(modelId: String): Model
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetModelByIdUseCase
 */
class GetModelByIdUseCaseImpl(private val modelRepository: ModelRepository) : GetModelByIdUseCase {
    override suspend fun invoke(modelId: String): Model {
        logger.debug { "GetModelByIdUseCase: Fetching model with ID: $modelId" }
        return modelRepository.getModelById(modelId)
    }
}
