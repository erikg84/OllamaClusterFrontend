package repository

import data.service.LLMApiService
import domain.model.*
import mu.KotlinLogging

/**
 * Repository interface for managing model data
 */
interface ModelRepository {
    /**
     * Get all models across the cluster
     * @return List of Model objects
     */
    suspend fun getAllModels(): List<Model>

    /**
     * Get models available on a specific node
     * @param nodeId The ID of the node
     * @return List of Model objects available on the specified node
     */
    suspend fun getModelsByNode(nodeId: String): List<Model>

    /**
     * Get a specific model by its ID
     * @param modelId The ID of the model to retrieve
     * @return Model object if found
     */
    suspend fun getModelById(modelId: String): Model
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of ModelRepository that uses LLMApiService to fetch model data
 */
class ModelRepositoryImpl(private val apiService: LLMApiService) : ModelRepository {

    override suspend fun getAllModels(): List<Model> {
        logger.debug { "Fetching all models" }
        return try {
            apiService.getAllModels()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching all models" }
            throw e
        }
    }

    override suspend fun getModelsByNode(nodeId: String): List<Model> {
        logger.debug { "Fetching models for node: $nodeId" }
        return try {
            apiService.getModelsByNode(nodeId)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching models for node: $nodeId" }
            throw e
        }
    }

    override suspend fun getModelById(modelId: String): Model {
        logger.debug { "Fetching model with ID: $modelId" }
        return try {
            apiService.getModelById(modelId)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching model with ID: $modelId" }
            throw e
        }
    }
}
