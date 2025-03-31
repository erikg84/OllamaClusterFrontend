package usecase

import mu.KotlinLogging
import repository.AdminRepository

/**
 * Use case for resetting system statistics
 */
interface ResetStatsUseCase {
    /**
     * Execute the use case to reset statistics
     * @return true if the operation was successful
     */
    suspend operator fun invoke(): Boolean
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of ResetStatsUseCase
 */
class ResetStatsUseCaseImpl(private val adminRepository: AdminRepository) : ResetStatsUseCase {
    override suspend fun invoke(): Boolean {
        logger.debug { "ResetStatsUseCase: Resetting system statistics" }
        val result = adminRepository.resetStats()
        logger.info { "Reset stats operation result: $result" }
        return result
    }
}
