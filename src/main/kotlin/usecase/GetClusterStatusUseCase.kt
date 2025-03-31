package usecase

import domain.model.ClusterStatus
import mu.KotlinLogging
import repository.ClusterRepository

/**
 * Use case for retrieving the overall status of the cluster
 */
interface GetClusterStatusUseCase {
    /**
     * Execute the use case to get the cluster status
     * @return ClusterStatus object with node and model information
     */
    suspend operator fun invoke(): ClusterStatus
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of GetClusterStatusUseCase
 */
class GetClusterStatusUseCaseImpl(private val clusterRepository: ClusterRepository) : GetClusterStatusUseCase {
    override suspend fun invoke(): ClusterStatus {
        logger.debug { "GetClusterStatusUseCase: Fetching cluster status" }
        return clusterRepository.getClusterStatus()
    }
}
