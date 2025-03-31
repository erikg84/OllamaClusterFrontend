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
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of LLMInteractionRepository that uses LLMApiService to interact with models
 */
class LLMInteractionRepositoryImpl(private val apiService: LLMApiService) : LLMInteractionRepository {

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
}
