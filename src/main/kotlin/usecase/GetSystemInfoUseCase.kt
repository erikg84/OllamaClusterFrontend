package usecase

import domain.model.SystemInfo
import mu.KotlinLogging
import repository.AdminRepository

/**
 * Use case for retrieving system information
 */
interface GetSystemInfoUseCase {
    /**
     * Execute the use case to get system information
     * @return SystemInfo object with detailed system information
     */
    suspend operator fun invoke(): SystemInfo
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetSystemInfoUseCase
 */
class GetSystemInfoUseCaseImpl(private val adminRepository: AdminRepository) : GetSystemInfoUseCase {
    override suspend fun invoke(): SystemInfo {
        logger.debug { "GetSystemInfoUseCase: Fetching system information" }
        return adminRepository.getSystemInfo()
    }
}
