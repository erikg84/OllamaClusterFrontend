package data.service

import domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Service interface for all LLM Cluster API operations
 */
interface LLMApiService {
    // Health check
    suspend fun checkHealth(): Boolean

    // Node management
    suspend fun getAllNodes(): List<Node>
    suspend fun getNodeStatus(): Map<String, NodeStatus>
    suspend fun getNodeById(nodeId: String): Node
    suspend fun getModelsByNode(nodeId: String): List<Model>

    // Cluster management
    suspend fun getClusterStatus(): ClusterStatus
    suspend fun getAllModels(): List<Model>
    suspend fun getModelById(modelId: String): Model

    // Queue management
    suspend fun getQueueStatus(): QueueStatus
    suspend fun pauseQueue(): Boolean
    suspend fun resumeQueue(): Boolean

    // LLM operations
    suspend fun chat(request: ChatRequest): ChatResponse
    suspend fun streamChat(request: ChatRequest): Flow<ChatResponse>
    suspend fun generate(request: GenerateRequest): GenerateResponse
    suspend fun streamGenerate(request: GenerateRequest): Flow<GenerateResponse>

    // Admin monitoring
    suspend fun getMetrics(): MetricsData
    suspend fun getSystemInfo(): SystemInfo
    suspend fun resetStats(): Boolean
    suspend fun getLogs(level: LogLevel? = null): List<LogEntry>
}
