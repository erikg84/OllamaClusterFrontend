package domain.model

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.ktor.client.call.*
import io.ktor.client.statement.*

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

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClusterStatus(
    @JsonProperty("totalNodes") val totalNodes: Int? = null,
    @JsonProperty("onlineNodes") val onlineNodes: Int? = null,
    @JsonProperty("totalModels") val totalModels: Int? = null,
    @JsonProperty("loadedModels") val loadedModels: Int? = null
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
data class LLMParameters(
    @JsonProperty("temperature") val temperature: Double? = null,
    @JsonProperty("topP") val topP: Double? = null,
    @JsonProperty("maxTokens") val maxTokens: Int? = null,
    @JsonProperty("frequencyPenalty") val frequencyPenalty: Double? = null
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
    @JsonProperty("errorRate") val errorRate: Double? = null
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
    @JsonProperty("status") val success: Boolean? = null,
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

suspend inline fun <reified T, R> HttpResponse.mapTo(crossinline transform: (T) -> R): R {
    val apiResponse: ApiResponseDto<T> = body()
    if (apiResponse.status == "ok" && apiResponse.data != null) {
        return transform(apiResponse.data)
    } else {
        throw Exception("API error: ${apiResponse.message}")
    }
}
