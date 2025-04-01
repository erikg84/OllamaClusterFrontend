package data.service

import domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import io.ktor.network.sockets.SocketTimeoutException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.timeout
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import util.asLineFlow

private val logger = KotlinLogging.logger {}

class LLMApiServiceImpl(private val httpClient: HttpClient) : LLMApiService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // API Endpoints
    private object Endpoints {
        // Health check
        const val HEALTH = "health"

        // Node management
        const val NODES = "api/nodes"
        const val NODES_STATUS = "api/nodes/status"
        fun nodeById(nodeId: String) = "api/nodes/$nodeId"
        fun modelsByNode(nodeId: String) = "api/nodes/$nodeId/models"

        // Cluster management
        const val CLUSTER_STATUS = "api/cluster/status"
        const val CLUSTER_MODELS = "api/cluster/models"
        fun modelById(modelId: String) = "api/cluster/models/$modelId"

        // Queue management
        const val QUEUE_STATUS = "api/queue/status"
        const val QUEUE_PAUSE = "api/queue/pause"
        const val QUEUE_RESUME = "api/queue/resume"

        // LLM operations
        const val CHAT = "api/chat"
        const val GENERATE = "api/generate"

        // Admin monitoring
        const val METRICS = "admin/metrics"
        const val SYSTEM_INFO = "admin/system"
        const val RESET_STATS = "admin/reset-stats"
        const val LOGS = "admin/logs"
    }

    override suspend fun checkHealth(): Boolean {
        return try {
            val response: HttpResponse = httpClient.get(Endpoints.HEALTH)
            response.status.isSuccess()
        } catch (e: Exception) {
            logger.error(e) { "Error checking API health" }
            false
        }
    }

    override suspend fun getAllNodes(): List<Node> {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.NODES)
            if (response.status.isSuccess()) {
                val apiResponse: ApiResponseDto<List<NodeDto>> = response.body()
                if (apiResponse.status == "ok" && apiResponse.data != null) {
                    return apiResponse.data.map { it.toDomainModel() }
                } else {
                    logger.error { "API returned error: ${apiResponse.message}" }
                    throw Exception("API error: ${apiResponse.message}")
                }
            } else {
                logger.error { "Error fetching nodes: ${response.status}" }
                throw Exception("Failed to fetch nodes: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching nodes" }
            throw e
        }
    }

    override suspend fun getNodeStatus(): Map<String, NodeStatus> {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.NODES_STATUS)
            if (response.status.isSuccess()) {
                val statusMap: Map<String, String> = response.body()
                return statusMap.mapValues { NodeStatus.fromString(it.value) }
            } else {
                logger.error { "Error fetching node status: ${response.status}" }
                throw Exception("Failed to fetch node status: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching node status" }
            throw e
        }
    }

    override suspend fun getNodeById(nodeId: String): Node {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.nodeById(nodeId))
            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error fetching node $nodeId: ${response.status}" }
                throw Exception("Failed to fetch node $nodeId: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching node $nodeId" }
            throw e
        }
    }

    override suspend fun getModelsByNode(nodeId: String): List<Model> {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.modelsByNode(nodeId))
            return response.mapTo<NodeModelsResponseDto, List<Model>> { nodeModelsDto ->
                nodeModelsDto.models.map { modelName ->
                    Model(
                        id = modelName,
                        name = modelName,
                        node = nodeId,
                        status = ModelStatus.LOADED,  // Default assumption
                        details = null  // You don't have details yet
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching models for node $nodeId" }
            throw e
        }
    }

    override suspend fun getClusterStatus(): ClusterStatus {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.CLUSTER_STATUS)
            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error fetching cluster status: ${response.status}" }
                throw Exception("Failed to fetch cluster status: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching cluster status" }
            throw e
        }
    }

    override suspend fun getAllModels(): List<Model> {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.CLUSTER_MODELS)

            // Parse the API response
            val apiResponse: ApiResponseDto<List<ModelSummaryDto>> = response.body()
            if (apiResponse.status == "ok" && apiResponse.data != null) {
                val modelSummaries = apiResponse.data

                // Create model instances for each node-model combination
                return modelSummaries.flatMap { summary ->
                    summary.nodes?.map { nodeName ->
                        Model(
                            id = summary.name,
                            name = summary.name,
                            node = nodeName,  // Set the node association
                            status = ModelStatus.LOADED,
                            details = null
                        )
                    } ?: emptyList()
                }
            } else {
                logger.error { "API returned error: ${apiResponse.message}" }
                throw Exception("API error: ${apiResponse.message}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching all models" }
            throw e
        }
    }

    override suspend fun getModelById(modelId: String): Model {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.modelById(modelId))
            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error fetching model $modelId: ${response.status}" }
                throw Exception("Failed to fetch model $modelId: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching model $modelId" }
            throw e
        }
    }

    override suspend fun getQueueStatus(): QueueStatus {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.QUEUE_STATUS)
            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error fetching queue status: ${response.status}" }
                throw Exception("Failed to fetch queue status: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching queue status" }
            throw e
        }
    }

    override suspend fun pauseQueue(): Boolean {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.QUEUE_PAUSE)
            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.success == true
            } else {
                logger.error { "Error pausing queue: ${response.status}" }
                throw Exception("Failed to pause queue: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error pausing queue" }
            throw e
        }
    }

    override suspend fun resumeQueue(): Boolean {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.QUEUE_RESUME)
            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.success == true
            } else {
                logger.error { "Error resuming queue: ${response.status}" }
                throw Exception("Failed to resume queue: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error resuming queue" }
            throw e
        }
    }

    override suspend fun chat(request: ChatRequest): ChatResponse {
        try {
            val nonStreamingRequest = request.copy(stream = false)
            val response: HttpResponse = httpClient.post(Endpoints.CHAT) {
                contentType(ContentType.Application.Json)
                setBody(nonStreamingRequest)
            }

            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error in chat request: ${response.status}" }
                throw Exception("Failed in chat request: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error in chat request" }
            throw e
        }
    }

    override suspend fun streamChat(request: ChatRequest): Flow<ChatResponse> {
        return flow {
            try {
                // Modify the request to explicitly set stream to false for now
                val nonStreamingRequest = request.copy(stream = false)

                logger.info { "Streaming is not yet supported. Falling back to regular request." }

                // Make a regular request
                val response = chat(nonStreamingRequest)

                // Emit the response as if it was streamed
                emit(response)
            } catch (e: Exception) {
                logger.error(e) { "Error in streaming chat request" }
                throw e
            }
        }
    }

    override suspend fun generate(request: GenerateRequest): GenerateResponse {
        try {
            val nonStreamingRequest = request.copy(stream = false)
            val response: HttpResponse = httpClient.post(Endpoints.GENERATE) {
                contentType(ContentType.Application.Json)
                setBody(nonStreamingRequest)
            }

            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error in generate request: ${response.status}" }
                throw Exception("Failed in generate request: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error in generate request" }
            throw e
        }
    }

    override suspend fun streamGenerate(request: GenerateRequest): Flow<GenerateResponse> {
        return flow {
            try {
                // Modify the request to explicitly set stream to false for now
                val nonStreamingRequest = request.copy(stream = false)

                logger.info { "Streaming is not yet supported. Falling back to regular request." }

                // Make a regular request
                val response = generate(nonStreamingRequest)

                // Emit the response as if it was streamed
                emit(response)
            } catch (e: Exception) {
                logger.error(e) { "Error in streaming generate request" }
                throw e
            }
        }
    }

    override suspend fun getMetrics(): MetricsData {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.METRICS) {
                timeout {
                    requestTimeoutMillis = 15000
                }
            }

            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error fetching metrics: ${response.status}" }

                // Check for 404 Not Found
                if (response.status == HttpStatusCode.NotFound) {
                    logger.warn { "Metrics endpoint not found, returning empty metrics" }
                    // Return empty metrics instead of throwing
                    return createEmptyMetrics()
                }

                throw Exception("Failed to fetch metrics: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching metrics" }

            // For connection errors, also return empty metrics
            if (e is ConnectTimeoutException || e is SocketTimeoutException) {
                logger.warn { "Connection timeout, returning empty metrics" }
                return createEmptyMetrics()
            }

            throw e
        }
    }

    private fun createEmptyMetrics(): MetricsData {
        return MetricsData(
            responseTimes = emptyMap(),
            requestCounts = emptyList(),
            nodePerformance = emptyMap(),
            modelPerformance = emptyMap()
        )
    }

    override suspend fun getSystemInfo(): SystemInfo {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.SYSTEM_INFO)
            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error fetching system info: ${response.status}" }
                throw Exception("Failed to fetch system info: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching system info" }
            throw e
        }
    }

    override suspend fun resetStats(): Boolean {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.RESET_STATS)
            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.success == true
            } else {
                logger.error { "Error resetting stats: ${response.status}" }
                throw Exception("Failed to reset stats: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error resetting stats" }
            throw e
        }
    }

    override suspend fun getLogs(level: LogLevel?): List<LogEntry> {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.LOGS) {
                if (level != null) {
                    parameter("level", level.toString().lowercase())
                }
            }

            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error fetching logs: ${response.status}" }
                throw Exception("Failed to fetch logs: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching logs" }
            throw e
        }
    }

    private suspend inline fun <reified T> HttpResponse.handleApiResponse(): T? {
        if (this.status.isSuccess()) {
            val apiResponse: ApiResponseDto<T> = this.body()
            if (apiResponse.status == "ok") {  // Check for "ok" string
                return apiResponse.data
            }
        }
        return null
    }

    private suspend inline fun <reified T, R> HttpResponse.mapTo(crossinline transform: (T) -> R): R {
        val apiResponse: ApiResponseDto<T> = body()
        if (apiResponse.status == "ok" && apiResponse.data != null) {
            return transform(apiResponse.data)
        } else {
            throw Exception("API error: ${apiResponse.message}")
        }
    }
}
