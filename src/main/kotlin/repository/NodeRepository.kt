package repository

import data.service.LLMApiService
import domain.model.Node
import domain.model.NodeStatus
import mu.KotlinLogging

/**
 * Repository interface for managing node data
 */
interface NodeRepository {
    /**
     * Get all nodes in the cluster
     * @return List of Node objects
     */
    suspend fun getAllNodes(): List<Node>

    /**
     * Get the status of all nodes
     * @return Map of node ID to NodeStatus
     */
    suspend fun getNodeStatus(): Map<String, NodeStatus>

    /**
     * Get a specific node by its ID
     * @param nodeId The ID of the node to retrieve
     * @return Node object if found
     */
    suspend fun getNodeById(nodeId: String): Node
}

private val logger = KotlinLogging.logger {}

/**
 * Implementation of NodeRepository that uses LLMApiService to fetch node data
 */
class NodeRepositoryImpl(private val apiService: LLMApiService) : NodeRepository {

    override suspend fun getAllNodes(): List<Node> {
        logger.debug { "Fetching all nodes" }
        return try {
            apiService.getAllNodes()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching all nodes" }
            throw e
        }
    }

    override suspend fun getNodeStatus(): Map<String, NodeStatus> {
        logger.debug { "Fetching status of all nodes" }
        return try {
            apiService.getNodeStatus()
        } catch (e: Exception) {
            logger.error(e) { "Error fetching node status" }
            throw e
        }
    }

    override suspend fun getNodeById(nodeId: String): Node {
        logger.debug { "Fetching node with ID: $nodeId" }
        return try {
            apiService.getNodeById(nodeId)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching node with ID: $nodeId" }
            throw e
        }
    }
}
