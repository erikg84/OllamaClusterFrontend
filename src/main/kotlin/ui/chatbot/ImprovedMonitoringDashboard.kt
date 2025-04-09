package ui.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domain.model.*
import ui.MatrixThemeColors

@Composable
fun ImprovedMonitoringDashboard(
    clusterStatus: ClusterStatus?,
    activeNodes: List<Node>,
    queueSize: Int,
    responseTimes: List<TimeSeriesPoint>,
    cpuUsage: Double,
    memoryUsage: Double,
    gpuUsage: Double,
    logs: List<LogEntry>,
    onRefresh: () -> Unit
) {
    val dashboardScroll = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp)
    ) {
        // Dashboard Header with Refresh Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYSTEM MONITOR",
                color = MatrixThemeColors.highlightColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh data",
                    tint = MatrixThemeColors.highlightColor
                )
            }
        }

        // Scrollable dashboard content
        LazyColumn(
            state = dashboardScroll,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cluster Status Card
            item {
                MonitoringCard(title = "Cluster Status") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Nodes online/total
                        BulletPoint(
                            text = "Nodes: ${activeNodes.size} online / ${clusterStatus?.totalNodes ?: "?"} total"
                        )

                        // Models loaded
                        BulletPoint(
                            text = "Models: ${clusterStatus?.loadedModels ?: "?"} loaded"
                        )

                        // Current queue
                        BulletPoint(
                            text = "Current queue: $queueSize requests"
                        )

                        // Display API version if available
                        if (clusterStatus != null) {
                            Divider(
                                color = MatrixThemeColors.separatorLine,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Text(
                                text = "LLM Cluster v3.2.1",
                                color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Active Nodes Card
            item {
                MonitoringCard(title = "Active Nodes") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeNodes.forEach { node ->
                            val nodeType = if (node.hardware?.gpu != null) "[GPU]" else "[CPU]"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BulletPoint(
                                    text = "${node.name ?: node.id} $nodeType",
                                    isActive = node.status == NodeStatus.ONLINE,
                                    modifier = Modifier.weight(1f)
                                )

                                // Show models loaded per node
                                Text(
                                    text = "${node.modelsLoaded ?: 0}",
                                    color = MatrixThemeColors.statusText,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (activeNodes.isEmpty()) {
                            Text(
                                text = "No active nodes",
                                color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Response Times Chart Card
            item {
                MonitoringCard(title = "Response Times (ms)") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                MatrixThemeColors.surface,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    ) {
                        if (responseTimes.isNotEmpty()) {
                            // Simple line chart implementation
                            ImprovedResponseTimesChart(responseTimes)
                        } else {
                            Text(
                                text = "No response time data available",
                                color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            // Resource Usage Card
            item {
                MonitoringCard(title = "Resource Usage") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // CPU Usage
                        ResourceUsageBar(
                            label = "CPU",
                            value = cpuUsage.toInt(),
                            maxValue = 100
                        )

                        // Memory Usage
                        ResourceUsageBar(
                            label = "MEM",
                            value = memoryUsage.toInt(),
                            maxValue = 100
                        )

                        // GPU Usage
                        ResourceUsageBar(
                            label = "GPU",
                            value = gpuUsage.toInt(),
                            maxValue = 100
                        )

                        // Queue
                        ResourceUsageBar(
                            label = "QUEUE",
                            value = queueSize,
                            maxValue = 10  // Assuming 10 is max queue size for display
                        )
                    }
                }
            }

            // System Logs Card
            item {
                MonitoringCard(title = "System Logs") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        logs.takeLast(5).forEach { log ->
                            LogEntryRow(log)
                        }

                        if (logs.isEmpty()) {
                            Text(
                                text = "No logs available",
                                color = MatrixThemeColors.statusText.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Performance Stats Card
            item {
                MonitoringCard(title = "Performance Stats") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DataRow(
                            label = "Avg. Response",
                            value = "526 ms"
                        )

                        DataRow(
                            label = "Tokens/Sec",
                            value = "145"
                        )

                        DataRow(
                            label = "Requests/Min",
                            value = "32"
                        )

                        DataRow(
                            label = "Error Rate",
                            value = "0.5%"
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}