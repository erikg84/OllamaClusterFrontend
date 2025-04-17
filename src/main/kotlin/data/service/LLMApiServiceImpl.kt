package data.service

import domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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
        const val VISION = "api/vision"
        const val VISION_MODELS = "api/vision-models"

        // Admin monitoring
        const val METRICS = "admin/metrics"
        const val SYSTEM_INFO = "admin/system"
        const val RESET_STATS = "admin/reset-stats"
        const val LOGS = "admin/logs"
        const val CLUSTER_METRICS = "api/cluster/metrics"

        // Performance optimization
        const val PERFORMANCE = "api/performance"
        fun optimizedParameters(model: String, taskType: String) = "api/performance/parameters/$model/$taskType"
        const val PREWARM = "api/performance/prewarm"
        const val CACHE_STATS = "api/performance/cache/stats"
        const val BATCH_STATS = "api/performance/batch/stats"

        // Workflow decomposition
        const val WORKFLOW = "api/workflow"
        const val DECOMPOSE_CHAT = "api/workflow/decompose/chat"
        const val DECOMPOSE_GENERATE = "api/workflow/decompose/generate"
        fun workflowById(id: String) = "api/workflow/$id"
        fun workflowNext(id: String) = "api/workflow/$id/next"
        fun workflowResult(id: String) = "api/workflow/$id/result"
        fun workflowTask(workflowId: String, taskId: String) = "api/workflow/$workflowId/task/$taskId"
        const val WORKFLOW_CLEANUP = "api/workflow/cleanup"

        // Agent conversations
        const val AGENTS = "api/agents/conversations"
        fun agentConversation(id: String) = "api/agents/conversations/$id"
        fun agentMessages(id: String) = "api/agents/conversations/$id/messages"
        const val AGENTS_CLEANUP = "api/agents/cleanup"

        // Orchestration patterns
        const val PATTERNS = "api/patterns"
        const val ENSEMBLE = "api/patterns/ensemble"
        const val DEBATE = "api/patterns/debate"
        const val MAESTRO = "api/patterns/maestro"
        fun execution(id: String) = "api/patterns/execution/$id"
        const val PATTERNS_CLEANUP = "api/patterns/cleanup"

        // Performance metrics
        const val PERFORMANCE_METRICS = "admin/performance"
        const val LOAD_BALANCING_METRICS = "admin/load-balancing"
        const val PROMETHEUS_METRICS = "admin/metrics/prometheus"
        const val CLUSTER_LOGS = "api/cluster/logs"
        const val RESET_CLUSTER_STATS = "api/cluster/reset-stats"
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
            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error fetching models for node $nodeId: ${response.status}" }
                throw Exception("Failed to fetch models for node $nodeId: ${response.status}")
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
            val models = response.handleApiResponse<List<Model>>()
            return models ?: emptyList()
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
                return apiResponse.status == "ok"
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
                return apiResponse.status == "ok"
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
                val streamingRequest = request.copy(stream = true)
                val response: HttpResponse = httpClient.post(Endpoints.CHAT) {
                    contentType(ContentType.Application.Json)
                    setBody(streamingRequest)
                    accept(ContentType.Text.EventStream)
                }

                if (!response.status.isSuccess()) {
                    throw Exception("Error in streaming chat request: ${response.status}")
                }

                // Process the server-sent events
                response.bodyAsChannel().asLineFlow().collect { chunk ->
                    val chunkText = chunk.toString()
                    if (chunkText.isNotBlank() && !chunkText.startsWith("data: [DONE]")) {
                        try {
                            // SSE format typically has "data: " prefix
                            val dataContent = if (chunkText.startsWith("data: ")) {
                                chunkText.substring(6)
                            } else {
                                chunkText
                            }

                            val chatResponse = json.decodeFromString<ChatResponse>(dataContent)
                            emit(chatResponse)
                        } catch (e: Exception) {
                            logger.error(e) { "Error parsing chat stream chunk: $chunkText" }
                        }
                    }
                }
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
                val streamingRequest = request.copy(stream = true)
                val response: HttpResponse = httpClient.post(Endpoints.GENERATE) {
                    contentType(ContentType.Application.Json)
                    setBody(streamingRequest)
                    accept(ContentType.Text.EventStream)
                }

                if (!response.status.isSuccess()) {
                    throw Exception("Error in streaming generate request: ${response.status}")
                }

                // Process the server-sent events
                response.bodyAsChannel().asLineFlow().collect { chunk ->
                    val chunkText = chunk.toString()
                    if (chunkText.isNotBlank() && !chunkText.startsWith("data: [DONE]")) {
                        try {
                            // SSE format typically has "data: " prefix
                            val dataContent = if (chunkText.startsWith("data: ")) {
                                chunkText.substring(6)
                            } else {
                                chunkText
                            }

                            val generateResponse = json.decodeFromString<GenerateResponse>(dataContent)
                            emit(generateResponse)
                        } catch (e: Exception) {
                            logger.error(e) { "Error parsing generate stream chunk: $chunkText" }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Error in streaming generate request" }
                throw e
            }
        }
    }

    override suspend fun getMetrics(): MetricsData {
        try {
            // Try the cluster metrics endpoint first
            try {
                logger.debug { "Requesting metrics from cluster endpoint: ${Endpoints.CLUSTER_METRICS}" }
                val response: HttpResponse = httpClient.get(Endpoints.CLUSTER_METRICS)
                if (response.status.isSuccess()) {
                    val metricsData: MetricsData = response.body()
                    logger.debug { "Received cluster metrics data" }
                    return metricsData
                }
            } catch (e: Exception) {
                logger.warn { "Failed to get cluster metrics, falling back to admin metrics: ${e.message}" }
            }

            // Fall back to admin metrics
            logger.debug { "Requesting metrics from admin endpoint: ${Endpoints.METRICS}" }
            val response: HttpResponse = httpClient.get(Endpoints.METRICS)
            if (response.status.isSuccess()) {
                return response.body()
            } else {
                logger.error { "Error fetching metrics: ${response.status}" }
                throw Exception("Failed to fetch metrics: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching metrics" }
            throw e
        }
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
                return apiResponse.status == "ok"
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
        return emptyList()
    }

    override suspend fun sendVisionRequest(request: VisionRequest): VisionResponse {
        return try {
            val response: HttpResponse = httpClient.post(Endpoints.VISION) {
                setBody(
                    MultiPartFormDataContent(
                    formData {
                        append("image", request.image.readBytes(), Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=${request.image.name}")
                            append(HttpHeaders.ContentType, ContentType.Image.Any.toString())
                        })
                        append("prompt", request.prompt)
                    }
                ))
            }

            if (response.status.isSuccess()) {
                response.body()
            } else {
                logger.error { "Error in vision request: ${response.status}" }
                VisionResponse(status = "error", message = "Failed with status: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Vision request failed" }
            VisionResponse(status = "error", message = "Request failed: ${e.message}")
        }
    }

    override suspend fun executeModelEnsemble(query: String, models: List<String>?, ensembleSize: Int): EnsembleResult {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.ENSEMBLE) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "query" to query,
                    "ensembleSize" to ensembleSize,
                    "models" to models
                ))
            }

            val result = response.handleApiResponse<EnsembleResult>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error executing model ensemble: empty or invalid response" }
                throw Exception("Failed to execute model ensemble: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error executing model ensemble" }
            throw e
        }
    }

    override suspend fun executeDebatePattern(query: String, models: List<String>?, debateRounds: Int): DebateResult {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.DEBATE) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "query" to query,
                    "debateRounds" to debateRounds,
                    "models" to models
                ))
            }

            val result = response.handleApiResponse<DebateResult>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error executing debate pattern: empty or invalid response" }
                throw Exception("Failed to execute debate pattern: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error executing debate pattern" }
            throw e
        }
    }

    override suspend fun executeMAESTROWorkflow(query: String, preferredModel: String?): MAESTROResult {
        try {
            val requestMap = mutableMapOf<String, Any?>(
                "query" to query
            )
            if (preferredModel != null) {
                requestMap["preferredModel"] = preferredModel
            }

            val response: HttpResponse = httpClient.post(Endpoints.MAESTRO) {
                contentType(ContentType.Application.Json)
                setBody(requestMap)
            }

            val result = response.handleApiResponse<MAESTROResult>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error executing MAESTRO workflow: empty or invalid response" }
                throw Exception("Failed to execute MAESTRO workflow: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error executing MAESTRO workflow" }
            throw e
        }
    }

    override suspend fun getExecutionStatus(executionId: String): ExecutionStatus {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.execution(executionId))

            val result = response.handleApiResponse<ExecutionStatus>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error getting execution status: empty or invalid response" }
                throw Exception("Failed to get execution status: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting execution status" }
            throw e
        }
    }

    override suspend fun cleanupExecutions(maxAgeMs: Long): Boolean {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.PATTERNS_CLEANUP) {
                contentType(ContentType.Application.Json)
                setBody(mapOf("maxAgeMs" to maxAgeMs))
            }

            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.status == "ok"
            } else {
                logger.error { "Error cleaning up executions: ${response.status}" }
                throw Exception("Failed to clean up executions: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error cleaning up executions" }
            throw e
        }
    }

    override suspend fun decomposeChat(request: ChatRequest): WorkflowInfo {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.DECOMPOSE_CHAT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val result = response.handleApiResponse<WorkflowInfo>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error decomposing chat request: empty or invalid response" }
                throw Exception("Failed to decompose chat request: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error decomposing chat request" }
            throw e
        }
    }

    override suspend fun decomposeGenerate(request: GenerateRequest): WorkflowInfo {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.DECOMPOSE_GENERATE) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val result = response.handleApiResponse<WorkflowInfo>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error decomposing generate request: empty or invalid response" }
                throw Exception("Failed to decompose generate request: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error decomposing generate request" }
            throw e
        }
    }

    override suspend fun getWorkflow(workflowId: String): WorkflowInfo {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.workflowById(workflowId))

            val result = response.handleApiResponse<WorkflowInfo>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error getting workflow: empty or invalid response" }
                throw Exception("Failed to get workflow: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting workflow" }
            throw e
        }
    }

    override suspend fun getNextTasks(workflowId: String): List<TaskInfo> {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.workflowNext(workflowId))

            val result = response.handleApiResponse<List<TaskInfo>>()
            return result ?: emptyList()
        } catch (e: Exception) {
            logger.error(e) { "Error getting next tasks" }
            throw e
        }
    }

    override suspend fun updateTaskStatus(
        workflowId: String,
        taskId: String,
        status: TaskStatus,
        result: Any?
    ): Boolean {
        try {
            val requestMap = mutableMapOf<String, Any?>(
                "status" to status.toString()
            )
            if (result != null) {
                requestMap["result"] = result
            }

            val response: HttpResponse = httpClient.put(Endpoints.workflowTask(workflowId, taskId)) {
                contentType(ContentType.Application.Json)
                setBody(requestMap)
            }

            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.status == "ok"
            } else {
                logger.error { "Error updating task status: ${response.status}" }
                throw Exception("Failed to update task status: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error updating task status" }
            throw e
        }
    }

    override suspend fun getFinalResult(workflowId: String): Any? {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.workflowResult(workflowId))

            if (response.status.isSuccess()) {
                return response.body<JsonObject>()
            } else {
                logger.error { "Error getting final result: ${response.status}" }
                throw Exception("Failed to get final result: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting final result" }
            throw e
        }
    }

    override suspend fun cleanupWorkflows(maxAgeMs: Long): Boolean {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.WORKFLOW_CLEANUP) {
                contentType(ContentType.Application.Json)
                setBody(mapOf("maxAgeMs" to maxAgeMs))
            }

            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.status == "ok"
            } else {
                logger.error { "Error cleaning up workflows: ${response.status}" }
                throw Exception("Failed to clean up workflows: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error cleaning up workflows" }
            throw e
        }
    }

    override suspend fun preWarmModel(model: String, nodeName: String?): Boolean {
        try {
            val requestMap = mutableMapOf<String, Any?>(
                "model" to model
            )
            if (nodeName != null) {
                requestMap["node"] = nodeName
            }

            val response: HttpResponse = httpClient.post(Endpoints.PREWARM) {
                contentType(ContentType.Application.Json)
                setBody(requestMap)
            }

            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.status == "ok"
            } else {
                logger.error { "Error pre-warming model: ${response.status}" }
                throw Exception("Failed to pre-warm model: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error pre-warming model" }
            throw e
        }
    }

    override suspend fun getOptimizedParameters(model: String, taskType: String): Map<String, Any> {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.optimizedParameters(model, taskType))

            val result = response.handleApiResponse<Map<String, Any>>()
            return result ?: emptyMap()
        } catch (e: Exception) {
            logger.error(e) { "Error getting optimized parameters" }
            throw e
        }
    }

    override suspend fun getCacheStats(): CacheStats {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.CACHE_STATS)

            val result = response.handleApiResponse<CacheStats>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error getting cache stats: empty or invalid response" }
                throw Exception("Failed to get cache stats: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting cache stats" }
            throw e
        }
    }

    override suspend fun getBatchQueueStats(): BatchQueueStats {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.BATCH_STATS)

            val result = response.handleApiResponse<BatchQueueStats>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error getting batch queue stats: empty or invalid response" }
                throw Exception("Failed to get batch queue stats: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting batch queue stats" }
            throw e
        }
    }

    override suspend fun createAgentConversation(query: String, model: String?): String {
        try {
            val requestMap = mutableMapOf<String, Any?>(
                "query" to query
            )
            if (model != null) {
                requestMap["model"] = model
            }

            val response: HttpResponse = httpClient.post(Endpoints.AGENTS) {
                contentType(ContentType.Application.Json)
                setBody(requestMap)
            }

            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Map<String, String>> = response.body()
                if (apiResponse.status == "ok" && apiResponse.data != null) {
                    return apiResponse.data["conversationId"]
                        ?: throw Exception("Missing conversationId in response")
                } else {
                    logger.error { "API returned error: ${apiResponse.message}" }
                    throw Exception("API error: ${apiResponse.message}")
                }
            } else {
                logger.error { "Error creating agent conversation: ${response.status}" }
                throw Exception("Failed to create agent conversation: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error creating agent conversation" }
            throw e
        }
    }

    override suspend fun getAgentConversation(conversationId: String): AgentConversation {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.agentConversation(conversationId))

            val result = response.handleApiResponse<AgentConversation>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error getting agent conversation: empty or invalid response" }
                throw Exception("Failed to get agent conversation: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting agent conversation" }
            throw e
        }
    }

    override suspend fun addAgentMessage(conversationId: String, message: String): Boolean {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.agentMessages(conversationId)) {
                contentType(ContentType.Application.Json)
                setBody(mapOf("message" to message))
            }

            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.status == "ok"
            } else {
                logger.error { "Error adding agent message: ${response.status}" }
                throw Exception("Failed to add agent message: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error adding agent message" }
            throw e
        }
    }

    override suspend fun cleanupAgentConversations(maxAgeMs: Long): Boolean {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.AGENTS_CLEANUP) {
                contentType(ContentType.Application.Json)
                setBody(mapOf("maxAgeMs" to maxAgeMs))
            }

            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.status == "ok"
            } else {
                logger.error { "Error cleaning up agent conversations: ${response.status}" }
                throw Exception("Failed to clean up agent conversations: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error cleaning up agent conversations" }
            throw e
        }
    }

    override suspend fun getPerformanceMetrics(): PerformanceMetrics {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.PERFORMANCE_METRICS)

            val result = response.handleApiResponse<PerformanceMetrics>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error getting performance metrics: empty or invalid response" }
                throw Exception("Failed to get performance metrics: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting performance metrics" }
            throw e
        }
    }

    override suspend fun getLoadBalancingMetrics(): LoadBalancingMetrics {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.LOAD_BALANCING_METRICS)

            val result = response.handleApiResponse<LoadBalancingMetrics>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error getting load balancing metrics: empty or invalid response" }
                throw Exception("Failed to get load balancing metrics: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting load balancing metrics" }
            throw e
        }
    }

    override suspend fun getPrometheusMetrics(): String {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.PROMETHEUS_METRICS)

            if (response.status.isSuccess()) {
                return response.bodyAsText()
            } else {
                logger.error { "Error getting Prometheus metrics: ${response.status}" }
                throw Exception("Failed to get Prometheus metrics: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting Prometheus metrics" }
            throw e
        }
    }

    override suspend fun getClusterLogs(level: LogLevel?): List<LogEntry> {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.CLUSTER_LOGS) {
                if (level != null) {
                    parameter("level", level.toString().lowercase())
                }
            }

            val result = response.handleApiResponse<List<LogEntry>>()
            return result ?: emptyList()
        } catch (e: Exception) {
            logger.error(e) { "Error getting cluster logs" }
            throw e
        }
    }

    override suspend fun resetClusterStats(): Boolean {
        try {
            val response: HttpResponse = httpClient.post(Endpoints.RESET_CLUSTER_STATS)

            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<Boolean> = response.body()
                return apiResponse.status == "ok"
            } else {
                logger.error { "Error resetting cluster stats: ${response.status}" }
                throw Exception("Failed to reset cluster stats: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error resetting cluster stats" }
            throw e
        }
    }

    override suspend fun getClusterMetrics(): ClusterMetrics {
        try {
            val response: HttpResponse = httpClient.get(Endpoints.CLUSTER_METRICS)

            val result = response.handleApiResponse<ClusterMetrics>()
            if (result != null) {
                return result
            } else {
                logger.error { "Error getting cluster metrics: empty or invalid response" }
                throw Exception("Failed to get cluster metrics: empty or invalid response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting cluster metrics" }
            throw e
        }
    }

    private suspend inline fun <reified T> HttpResponse.handleApiResponse(): T? {
        if (this.status.isSuccess()) {
            val apiResponse: ApiResponse<T> = this.body()
            if (apiResponse.status == "ok") {
                return apiResponse.data
            }
        }
        return null
    }
}
