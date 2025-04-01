package ui.screen.metrics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodel.MetricsViewModel

@Composable
fun MetricsScreen(viewModel: MetricsViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadMetrics()
    }

    val errors by viewModel.errors.collectAsState(initial = null)
    val nodes by viewModel.nodes.collectAsState()
    val models by viewModel.models.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val autoRefreshEnabled by viewModel.autoRefreshEnabled.collectAsState()
    val autoRefreshInterval by viewModel.autoRefreshInterval.collectAsState()
    val responseTimeData by viewModel.responseTimeData.collectAsState()
    val requestCountData by viewModel.requestCountData.collectAsState()
    val nodePerformance by viewModel.nodePerformance.collectAsState()
    val modelPerformance by viewModel.modelPerformance.collectAsState()

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    errors?.let { errorMessage ->
        if (responseTimeData.isEmpty() &&
            requestCountData.isEmpty() &&
            nodePerformance.isEmpty() &&
            modelPerformance.isEmpty()) {

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadMetrics() }) {
                        Text("Retry")
                    }
                }
            }
            return
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilterSection(
            nodes = nodes,
            models = models,
            selectedNode = selectedNode,
            selectedModel = selectedModel,
            selectedTimeRange = selectedTimeRange,
            autoRefreshEnabled = autoRefreshEnabled,
            autoRefreshInterval = autoRefreshInterval,
            onNodeSelected = { viewModel.setNodeFilter(it) },
            onModelSelected = { viewModel.setModelFilter(it) },
            onTimeRangeSelected = { viewModel.setTimeRange(it) },
            onAutoRefreshToggled = { viewModel.toggleAutoRefresh() },
            onAutoRefreshIntervalChanged = { viewModel.setAutoRefreshInterval(it) },
            onExportMetrics = { /* TODO: handle export */ }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Response Time (ms)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        ResponseTimeChart(
                            data = responseTimeData,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Request Count",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        RequestCountChart(
                            data = requestCountData,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NodePerformanceSection(
                nodePerformance = nodePerformance,
                modifier = Modifier.weight(1f)
            )

            ModelPerformanceSection(
                modelPerformance = modelPerformance,
                modifier = Modifier.weight(1f)
            )

            Card(
                modifier = Modifier.weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Request Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (nodePerformance.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No data available")
                            }
                        } else {
                            RequestDistributionChart(
                                data = nodePerformance.mapValues {
                                    it.value.requestsProcessed?.toDouble() ?: 0.0
                                },
                                totalRequests = nodePerformance.values.sumOf {
                                    it.requestsProcessed ?: 0
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
