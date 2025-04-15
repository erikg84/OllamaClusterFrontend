package usecase

import domain.model.VisionRequest
import domain.model.VisionResponse
import mu.KotlinLogging
import repository.LLMInteractionRepository

interface SendVisionRequestUseCase {
    suspend operator fun invoke(request: VisionRequest): VisionResponse
}

private val logger = KotlinLogging.logger {}

class SendVisionRequestUseCaseImpl(
    private val repository: LLMInteractionRepository
) : SendVisionRequestUseCase {
    override suspend fun invoke(request: VisionRequest): VisionResponse {
        logger.debug { "Sending VisionRequest request: $request" }
        return repository.sendVisionRequest(request)
    }
}

