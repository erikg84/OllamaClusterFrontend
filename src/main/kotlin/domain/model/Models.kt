package domain.model

import com.fasterxml.jackson.annotation.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import java.io.File

@JsonIgnoreProperties(ignoreUnknown = true)
data class Node(
    @JsonProperty("id") val id: String? = null,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("status") val status: NodeStatus? = null,
    @JsonProperty("modelsLoaded") val modelsLoaded: Int? = null,
    @JsonProperty("hardware") val hardware: Hardware? = null
) {
    val isOnline: Boolean
        get() = status?.isOnline ?: false

    val isOffline: Boolean
        get() = status?.isOffline ?: true
}

@JsonIgnoreProperties(ignoreUnknown = true)
enum class NodeStatus {
    ONLINE, OFFLINE;

    val isOnline: Boolean
        get() = this == ONLINE

    val isOffline: Boolean
        get() = this == OFFLINE

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(status: String?): NodeStatus = when (status?.lowercase()) {
            "online" -> ONLINE
            else -> OFFLINE
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Hardware(
    @JsonProperty("cpu") val cpu: String? = null,
    @JsonProperty("gpu") val gpu: String? = null,
    @JsonProperty("memory") val memory: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Model(
    @JsonProperty("id") val id: String? = null,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("node") val node: String? = null,
    @JsonProperty("status") val status: ModelStatus? = null,
    @JsonProperty("details") val details: ModelDetails? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModelSummaryDto(
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("nodeCount") val nodeCount: Int? = null,
    @JsonProperty("nodes") val nodes: List<String>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
enum class ModelStatus {
    LOADED, LOADING, UNLOADED;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(status: String?): ModelStatus = when (status?.lowercase()) {
            "loaded" -> LOADED
            "loading" -> LOADING
            else -> UNLOADED
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModelDetails(
    @JsonProperty("parameters") val parameters: Long? = null,
    @JsonProperty("contextLength") val contextLength: Int? = null,
    @JsonProperty("type") val type: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class QueueStatus(
    @JsonProperty("active") val active: Boolean? = null,
    @JsonProperty("pending") val pending: Int? = null,
    @JsonProperty("processing") val processing: Int? = null,
    @JsonProperty("completed") val completed: Int? = null
)

// Add this to your ClusterStatus model or create a separate method to extract this
@JsonIgnoreProperties(ignoreUnknown = true)
data class ClusterStatus(
    @JsonProperty("totalNodes") val totalNodes: Int? = null,
    @JsonProperty("availableNodes") val onlineNodes: Int? = null,
    @JsonProperty("totalModels") val totalModels: Int? = null,
    @JsonProperty("loadedModels") val loadedModels: Int? = null,
    @JsonProperty("nodesStatus") val nodesStatus: Map<String, String>? = null,
    @JsonProperty("nodeQueueSizes") val nodeQueueSizes: Map<String, Int>? = null,
    @JsonProperty("modelsAvailable") val modelsAvailable: Map<String, List<String>>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatMessage(
    @JsonProperty("role") val role: MessageRole? = null,
    @JsonProperty("content") val content: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
enum class MessageRole {
    @JsonProperty("user") USER,
    @JsonProperty("assistant") ASSISTANT,
    @JsonProperty("system") SYSTEM
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatRequest(
    @JsonProperty("node") val node: String? = null,
    @JsonProperty("model") val model: String? = null,
    @JsonProperty("messages") val messages: List<ChatMessage>? = null,
    @JsonProperty("stream") val stream: Boolean? = null,
    @JsonProperty("parameters") val parameters: LLMParameters? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GenerateRequest(
    @JsonProperty("node") val node: String? = null,
    @JsonProperty("model") val model: String? = null,
    @JsonProperty("prompt") val prompt: String? = null,
    @JsonProperty("stream") val stream: Boolean? = null,
    @JsonProperty("parameters") val parameters: LLMParameters? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatResponse(
    @JsonProperty("message") val message: ChatMessage? = null,
    @JsonProperty("usage") val usage: TokenUsage? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GenerateResponse(
    @JsonProperty("text") val text: String? = null,
    @JsonProperty("usage") val usage: TokenUsage? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenUsage(
    @JsonProperty("promptTokens") val promptTokens: Int? = null,
    @JsonProperty("completionTokens") val completionTokens: Int? = null,
    @JsonProperty("totalTokens") val totalTokens: Int? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SystemInfo(
    @JsonProperty("apiVersion") val apiVersion: String? = null,
    @JsonProperty("uptime") val uptime: String? = null,
    @JsonProperty("cpuUsage") val cpuUsage: Double? = null,
    @JsonProperty("memoryUsage") val memoryUsage: MemoryUsage? = null,
    @JsonProperty("diskUsage") val diskUsage: DiskUsage? = null,
    @JsonProperty("nodeJsVersion") val nodeJsVersion: String? = null,
    @JsonProperty("expressVersion") val expressVersion: String? = null,
    @JsonProperty("environment") val environment: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MemoryUsage(
    @JsonProperty("used") val used: String? = null,
    @JsonProperty("total") val total: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DiskUsage(
    @JsonProperty("used") val used: String? = null,
    @JsonProperty("total") val total: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MetricsData(
    @JsonProperty("responseTimes") val responseTimes: Map<String, List<TimeSeriesPoint>>? = null,
    @JsonProperty("requestCounts") val requestCounts: List<TimeSeriesPoint>? = null,
    @JsonProperty("nodePerformance") val nodePerformance: Map<String, NodePerformanceMetrics>? = null,
    @JsonProperty("modelPerformance") val modelPerformance: Map<String, ModelPerformanceMetrics>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TimeSeriesPoint(
    @JsonProperty("time") val time: String? = null,
    @JsonProperty("value") val value: Double? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NodePerformanceMetrics(
    @JsonProperty("avgResponseTime") val avgResponseTime: Double? = null,
    @JsonProperty("requestsProcessed") val requestsProcessed: Int? = null,
    @JsonProperty("errorRate") val errorRate: Double? = null,
    @JsonProperty("requestCount") val requestCount: Int,
    @JsonProperty("cpuUsage") val cpuUsage: Double,
    @JsonProperty("memoryUsage") val memoryUsage: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModelPerformanceMetrics(
    @JsonProperty("avgResponseTime") val avgResponseTime: Double? = null,
    @JsonProperty("requestsProcessed") val requestsProcessed: Int? = null,
    @JsonProperty("avgTokensGenerated") val avgTokensGenerated: Double? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogEntry(
    @JsonProperty("timestamp") val timestamp: String? = null,
    @JsonProperty("level") val level: LogLevel? = null,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("source") val source: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
enum class LogLevel {
    @JsonProperty("info") INFO,
    @JsonProperty("warn") WARN,
    @JsonProperty("error") ERROR,
    @JsonProperty("debug") DEBUG
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiResponse<T>(
    @JsonProperty("status") val status: String? = null,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("data") val data: T? = null
)

// Backend response DTO
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiResponseDto<T>(
    @JsonProperty("status") val status: String? = null,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("data") val data: T? = null
)

// Backend node DTO
@JsonIgnoreProperties(ignoreUnknown = true)
data class NodeDto(
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("host") val host: String? = null,
    @JsonProperty("port") val port: Int? = null,
    @JsonProperty("type") val type: String? = null,
    @JsonProperty("platform") val platform: String? = null,
    @JsonProperty("capabilities") val capabilities: List<String>? = null
)

fun NodeDto.toDomainModel(): Node {
    return Node(
        id = name,
        name = name,
        status = NodeStatus.fromString("online"), // You'll need to get status from elsewhere
        modelsLoaded = 0, // You'll need to calculate this
        hardware = Hardware(
            cpu = null, // You'll need additional data
            gpu = null,
            memory = null
        )
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class NodeModelsResponseDto(
    @JsonProperty("node") val node: String,
    @JsonProperty("models") val models: List<String>
)

fun updateNodesWithModelsLoaded(nodes: List<Node>, clusterStatus: ClusterStatus): List<Node> {
    return nodes.map { node ->
        val nodeId = node.id ?: node.name
        if (nodeId != null) {
            val modelsForNode = clusterStatus.modelsAvailable?.get(nodeId)
            node.copy(modelsLoaded = modelsForNode?.size ?: 0)
        } else {
            node
        }
    }
}

suspend inline fun <reified T, R> HttpResponse.mapTo(crossinline transform: (T) -> R): R {
    val apiResponse: ApiResponseDto<T> = body()
    if (apiResponse.status == "ok" && apiResponse.data != null) {
        return transform(apiResponse.data)
    } else {
        throw Exception("API error: ${apiResponse.message}")
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
data class EnsembleResult(
    val id: String,
    val consensusOutput: String,
    val completions: List<EnsembleCompletion>,
    val executionTimeMs: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EnsembleCompletion(
    val model: String,
    val text: String,
    val score: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DebateResult(
    val id: String,
    val finalSynthesis: String,
    val debateMessages: List<DebateMessage>,
    val executionTimeMs: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DebateMessage(
    val model: String,
    val content: String,
    val round: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MAESTROResult(
    val id: String,
    val conversationId: String,
    val finalOutput: String,
    val agents: List<MAESTROAgent>,
    val executionTimeMs: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MAESTROAgent(
    val agentType: String,
    val content: String,
    val timestamp: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExecutionStatus(
    val id: String,
    val pattern: String,
    val query: String,
    val models: List<String>,
    val state: String,
    val startTime: Long,
    val endTime: Long?,
    val result: Any?
)

// Task decomposition models
@JsonIgnoreProperties(ignoreUnknown = true)
enum class TaskStatus {
    PENDING, READY, RUNNING, COMPLETED, FAILED
}

@JsonIgnoreProperties(ignoreUnknown = true)
enum class TaskType {
    REASONING, RESEARCH, CREATIVE, CODE, SYNTHESIS, EXECUTION
}

@JsonIgnoreProperties(ignoreUnknown = true)
enum class DecompositionStrategy {
    SEQUENTIAL, PARALLEL, HIERARCHICAL, DYNAMIC
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaskInfo(
    val id: String,
    val type: TaskType,
    val content: String,
    val status: TaskStatus,
    val dependencies: List<String>,
    val startedAt: Long? = null,
    val completedAt: Long? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowInfo(
    val id: String,
    val decompositionStrategy: DecompositionStrategy,
    val createdAt: Long,
    val completedAt: Long? = null,
    val tasks: List<TaskInfo>
)

// Performance optimization models
@JsonIgnoreProperties(ignoreUnknown = true)
data class CacheStats(
    val entries: Int,
    val hitRate: Double,
    val missRate: Double,
    val avgResponseSize: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BatchQueueStats(
    val activeQueues: Int,
    val totalQueuedRequests: Int,
    val avgProcessingTimeMs: Int,
    val avgBatchSize: Double
)

// Agent conversation models
@JsonIgnoreProperties(ignoreUnknown = true)
data class AgentConversation(
    val id: String,
    val state: String,
    val initialQuery: String,
    val messages: List<AgentMessage>,
    val createdAt: Long,
    val completedAt: Long? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AgentMessage(
    val role: String,
    val content: String,
    val timestamp: Long
)

// Additional admin models
@JsonIgnoreProperties(ignoreUnknown = true)
data class PerformanceMetrics(
    val modelMetrics: Map<String, ModelMetrics>,
    val nodeMetrics: Map<String, NodePerformanceMetrics>,
    val systemMetrics: SystemPerformanceMetrics
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModelMetrics(
    val requestCount: Int,
    val avgResponseTimeMs: Double,
    val avgTokensPerSecond: Double,
    val errorRate: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SystemPerformanceMetrics(
    val totalRequests: Int,
    val requestsPerMinute: Double,
    val avgResponseTimeMs: Double,
    val p95ResponseTimeMs: Double,
    val p99ResponseTimeMs: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LoadBalancingMetrics(
    val nodeMetrics: Map<String, NodeLoadMetrics>,
    val routingDecisions: List<RoutingDecision>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NodeLoadMetrics(
    val node: String,
    val currentLoad: Double,
    val capacity: Double,
    val activeRequests: Int,
    val requestsServed: Int,
    val avgResponseTimeMs: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RoutingDecision(
    val timestamp: Long,
    val requestId: String,
    val model: String,
    val selectedNode: String,
    val reason: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClusterMetrics(
    val nodeMetrics: Map<String, NodeMetrics>,
    val systemInfo: Map<String, SystemInfo>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VisionModelResponse(
    val status: String,
    val message: String,
    val data: List<VisionModel>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VisionModel(
    val id: String,
    val name: String,
    val type: String,
    val size: Long,
    val quantization: String,
    val status: String,
    val node: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NodeMetrics(
    val requestCounts: Map<String, Long>,
    val nodePerformance: Map<String, NodePerf>,
    val modelPerformance: Map<String, ModelPerf>,
    val responseTimes: Map<String, List<TimePoint>>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NodePerf(
    val avgResponseTime: Double = 0.0,
    val requestsProcessed: Long = 0,
    val errorRate: Double = 0.0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModelPerf(
    val avgResponseTime: Double = 0.0,
    val requestsProcessed: Long = 0,
    val avgTokensGenerated: Double = 0.0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TimePoint(
    val time: String = "",
    val value: Double = 0.0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VisionRequest(
    val image: File,
    val prompt: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VisionResponse(
    val status: String = "",
    val message: String = "",
    val data: VisionResponseData? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VisionResponseData(
    val model: String = "",
    val node: String = "",
    val content: String = "",
    val metrics: VisionMetrics? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VisionMetrics(
    val total_duration_ms: Long = 0,
    val prompt_eval_count: Int = 0,
    val completion_token_count: Int = 0
)