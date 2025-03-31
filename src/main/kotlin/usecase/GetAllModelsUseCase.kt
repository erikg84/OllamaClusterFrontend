package usecase

import domain.model.*
import mu.KotlinLogging
import repository.ModelRepository

/**
 * Use case for retrieving all models in the cluster
 */
interface GetAllModelsUseCase {
    /**
     * Execute the use case to get all models
     * @return List of Model objects
     */
    suspend operator fun invoke(): List<Model>
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetAllModelsUseCase
 */
class GetAllModelsUseCaseImpl(private val modelRepository: ModelRepository) : GetAllModelsUseCase {
    override suspend fun invoke(): List<Model> {
        logger.debug { "GetAllModelsUseCase: Fetching all models" }
        return modelRepository.getAllModels()
    }
}
