package usecase

import domain.model.GenerateRequest
import domain.model.GenerateResponse
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import repository.LLMInteractionRepository

/**
 * Use case for generating text with LLM models
 */
interface GenerateTextUseCase {
    /**
     * Execute the use case to send a text generation request and get a response
     * @param request GenerateRequest containing node, model, prompt, and parameters
     * @param stream Whether to stream the response
     * @return Either a single GenerateResponse or a Flow of GenerateResponse objects if streaming
     */
    suspend operator fun invoke(request: GenerateRequest, stream: Boolean = false): Any

    /**
     * Execute the use case with streaming
     * @param request GenerateRequest containing node, model, prompt, and parameters
     * @return Flow of GenerateResponse objects
     */
    suspend fun stream(request: GenerateRequest): Flow<GenerateResponse>
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GenerateTextUseCase
 */
class GenerateTextUseCaseImpl(private val llmInteractionRepository: LLMInteractionRepository) : GenerateTextUseCase {
    override suspend fun invoke(request: GenerateRequest, stream: Boolean): Any {
        return if (stream) {
            stream(request)
        } else {
            logger.debug { "GenerateTextUseCase: Sending generate request to ${request.node}/${request.model}" }
            llmInteractionRepository.generate(request)
        }
    }

    override suspend fun stream(request: GenerateRequest): Flow<GenerateResponse> {
        logger.debug { "GenerateTextUseCase: Sending streaming generate request to ${request.node}/${request.model}" }
        return llmInteractionRepository.streamGenerate(request)
    }
}
