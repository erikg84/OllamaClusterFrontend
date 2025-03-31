package usecase

import domain.model.MetricsData
import mu.KotlinLogging
import repository.AdminRepository

/**
 * Use case for retrieving system performance metrics
 */
interface GetSystemMetricsUseCase {
    /**
     * Execute the use case to get system metrics
     * @return MetricsData object with performance metrics
     */
    suspend operator fun invoke(): MetricsData
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetSystemMetricsUseCase
 */
class GetSystemMetricsUseCaseImpl(private val adminRepository: AdminRepository) : GetSystemMetricsUseCase {
    override suspend fun invoke(): MetricsData {
        logger.debug { "GetSystemMetricsUseCase: Fetching system metrics" }
        return adminRepository.getMetrics()
    }
}
