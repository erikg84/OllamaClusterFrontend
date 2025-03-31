package usecase

import domain.model.ChatRequest
import domain.model.ChatResponse
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import repository.LLMInteractionRepository

/**
 * Use case for sending chat requests to LLM models
 */
interface ChatWithLLMUseCase {
    /**
     * Execute the use case to send a chat request and get a response
     * @param request ChatRequest containing node, model, messages, and parameters
     * @param stream Whether to stream the response
     * @return Either a single ChatResponse or a Flow of ChatResponse objects if streaming
     */
    suspend operator fun invoke(request: ChatRequest, stream: Boolean = false): Any

    /**
     * Execute the use case with streaming
     * @param request ChatRequest containing node, model, messages, and parameters
     * @return Flow of ChatResponse objects
     */
    suspend fun stream(request: ChatRequest): Flow<ChatResponse>
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of ChatWithLLMUseCase
 */
class ChatWithLLMUseCaseImpl(private val llmInteractionRepository: LLMInteractionRepository) : ChatWithLLMUseCase {
    override suspend fun invoke(request: ChatRequest, stream: Boolean): Any {
        return if (stream) {
            stream(request)
        } else {
            logger.debug { "ChatWithLLMUseCase: Sending chat request to ${request.node}/${request.model}" }
            llmInteractionRepository.chat(request)
        }
    }

    override suspend fun stream(request: ChatRequest): Flow<ChatResponse> {
        logger.debug { "ChatWithLLMUseCase: Sending streaming chat request to ${request.node}/${request.model}" }
        return llmInteractionRepository.streamChat(request)
    }
}
