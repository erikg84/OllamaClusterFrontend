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

    // Advanced orchestration patterns
    suspend fun executeModelEnsemble(query: String, models: List<String>?, ensembleSize: Int = 3): EnsembleResult
    suspend fun executeDebatePattern(query: String, models: List<String>?, debateRounds: Int = 3): DebateResult
    suspend fun executeMAESTROWorkflow(query: String, preferredModel: String? = null): MAESTROResult
    suspend fun getExecutionStatus(executionId: String): ExecutionStatus
    suspend fun cleanupExecutions(maxAgeMs: Long = 24 * 60 * 60 * 1000): Boolean

    // Task decomposition
    suspend fun decomposeChat(request: ChatRequest): WorkflowInfo
    suspend fun decomposeGenerate(request: GenerateRequest): WorkflowInfo
    suspend fun getWorkflow(workflowId: String): WorkflowInfo
    suspend fun getNextTasks(workflowId: String): List<TaskInfo>
    suspend fun updateTaskStatus(workflowId: String, taskId: String, status: TaskStatus, result: Any? = null): Boolean
    suspend fun getFinalResult(workflowId: String): Any?
    suspend fun cleanupWorkflows(maxAgeMs: Long = 24 * 60 * 60 * 1000): Boolean

    // Performance optimization
    suspend fun preWarmModel(model: String, nodeName: String? = null): Boolean
    suspend fun getOptimizedParameters(model: String, taskType: String): Map<String, Any>
    suspend fun getCacheStats(): CacheStats
    suspend fun getBatchQueueStats(): BatchQueueStats

    // Agent-based operations
    suspend fun createAgentConversation(query: String, model: String? = null): String
    suspend fun getAgentConversation(conversationId: String): AgentConversation
    suspend fun addAgentMessage(conversationId: String, message: String): Boolean
    suspend fun cleanupAgentConversations(maxAgeMs: Long = 24 * 60 * 60 * 1000): Boolean

    // Additional monitoring endpoints
    suspend fun getPerformanceMetrics(): PerformanceMetrics
    suspend fun getLoadBalancingMetrics(): LoadBalancingMetrics
    suspend fun getPrometheusMetrics(): String
    suspend fun getClusterLogs(level: LogLevel? = null): List<LogEntry>
    suspend fun resetClusterStats(): Boolean
    suspend fun getClusterMetrics(): ClusterMetrics

    // Vision
    suspend fun sendVisionRequest(request: VisionRequest): VisionResponse
}
