package repository

import data.service.LLMApiService
import domain.model.*
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging

/**
 * Repository interface for interacting with LLM models
 */
interface LLMInteractionRepository {
    /**
     * Send a chat request to a specific LLM model
     * @param request ChatRequest object with node, model, messages, and parameters
     * @return ChatResponse object with the model's response
     */
    suspend fun chat(request: ChatRequest): ChatResponse

    /**
     * Send a streaming chat request to a specific LLM model
     * @param request ChatRequest object with node, model, messages, and parameters
     * @return Flow of ChatResponse objects with incremental responses
     */
    suspend fun streamChat(request: ChatRequest): Flow<ChatResponse>

    /**
     * Send a text generation request to a specific LLM model
     * @param request GenerateRequest object with node, model, prompt, and parameters
     * @return GenerateResponse object with the generated text
     */
    suspend fun generate(request: GenerateRequest): GenerateResponse

    /**
     * Send a streaming text generation request to a specific LLM model
     * @param request GenerateRequest object with node, model, prompt, and parameters
     * @return Flow of GenerateResponse objects with incremental generated text
     */
    suspend fun streamGenerate(request: GenerateRequest): Flow<GenerateResponse>

    /**
     * Execute a model ensemble pattern
     * @param query The input query
     * @param models Optional list of models to use
     * @param ensembleSize Number of models to include in the ensemble
     * @return EnsembleResult with consensus output
     */
    suspend fun executeModelEnsemble(
        query: String,
        models: List<String>? = null,
        ensembleSize: Int = 3
    ): EnsembleResult

    /**
     * Execute a debate pattern between models
     * @param query The input query
     * @param models Optional list of models to debate
     * @param debateRounds Number of debate rounds
     * @return DebateResult with final synthesis
     */
    suspend fun executeDebatePattern(
        query: String,
        models: List<String>? = null,
        debateRounds: Int = 3
    ): DebateResult

    /**
     * Execute a MAESTRO workflow
     * @param query The input query
     * @param preferredModel Optional preferred model
     * @return MAESTROResult with workflow execution details
     */
    suspend fun executeMAESTROWorkflow(
        query: String,
        preferredModel: String? = null
    ): MAESTROResult

    /**
     * Send a vision request with an image file to a model
     */
    suspend fun sendVisionRequest(request: VisionRequest): VisionResponse

}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of LLMInteractionRepository that uses LLMApiService to interact with models
 */
class LLMInteractionRepositoryImpl(private val apiService: LLMApiService) : LLMInteractionRepository {

    override suspend fun sendVisionRequest(request: VisionRequest): VisionResponse {
        return try {
            val response = apiService.sendVisionRequest(request)
            response
        } catch (e: Exception) {
            throw e
        }
    }


    override suspend fun chat(request: ChatRequest): ChatResponse {
        logger.debug { "Sending chat request to node: ${request.node}, model: ${request.model}" }
        return try {
            val response = apiService.chat(request)
            logger.debug { "Received chat response from model: ${request.model}" }
            response
        } catch (e: Exception) {
            logger.error(e) { "Error during chat request to node: ${request.node}, model: ${request.model}" }
            throw e
        }
    }

    override suspend fun streamChat(request: ChatRequest): Flow<ChatResponse> {
        logger.debug { "Sending streaming chat request to node: ${request.node}, model: ${request.model}" }
        return try {
            val responseFlow = apiService.streamChat(request)
            logger.debug { "Established streaming chat connection with node: ${request.node}, model: ${request.model}" }
            responseFlow
        } catch (e: Exception) {
            logger.error(e) { "Error during streaming chat request to node: ${request.node}, model: ${request.model}" }
            throw e
        }
    }

    override suspend fun generate(request: GenerateRequest): GenerateResponse {
        logger.debug { "Sending generate request to node: ${request.node}, model: ${request.model}" }
        return try {
            val response = apiService.generate(request)
            logger.debug { "Received generate response from model: ${request.model}" }
            response
        } catch (e: Exception) {
            logger.error(e) { "Error during generate request to node: ${request.node}, model: ${request.model}" }
            throw e
        }
    }

    override suspend fun streamGenerate(request: GenerateRequest): Flow<GenerateResponse> {
        logger.debug { "Sending streaming generate request to node: ${request.node}, model: ${request.model}" }
        return try {
            val responseFlow = apiService.streamGenerate(request)
            logger.debug { "Established streaming generate connection with node: ${request.node}, model: ${request.model}" }
            responseFlow
        } catch (e: Exception) {
            logger.error(e) { "Error during streaming generate request to node: ${request.node}, model: ${request.model}" }
            throw e
        }
    }

    override suspend fun executeModelEnsemble(
        query: String,
        models: List<String>?,
        ensembleSize: Int
    ): EnsembleResult {
        logger.debug { "Executing model ensemble with query: $query" }
        return try {
            apiService.executeModelEnsemble(query, models, ensembleSize)
        } catch (e: Exception) {
            logger.error(e) { "Error executing model ensemble" }
            throw e
        }
    }

    override suspend fun executeDebatePattern(
        query: String,
        models: List<String>?,
        debateRounds: Int
    ): DebateResult {
        logger.debug { "Executing debate pattern with query: $query" }
        return try {
            apiService.executeDebatePattern(query, models, debateRounds)
        } catch (e: Exception) {
            logger.error(e) { "Error executing debate pattern" }
            throw e
        }
    }

    override suspend fun executeMAESTROWorkflow(
        query: String,
        preferredModel: String?
    ): MAESTROResult {
        logger.debug { "Executing MAESTRO workflow with query: $query" }
        return try {
            apiService.executeMAESTROWorkflow(query, preferredModel)
        } catch (e: Exception) {
            logger.error(e) { "Error executing MAESTRO workflow" }
            throw e
        }
    }
}
