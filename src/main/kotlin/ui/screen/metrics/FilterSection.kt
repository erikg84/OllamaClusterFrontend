package ui.screen.metrics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Model
import domain.model.Node
import viewmodel.MetricsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    nodes: List<Node>,
    models: List<Model>,
    selectedNode: String?,
    selectedModel: String?,
    selectedTimeRange: MetricsViewModel.TimeRange,
    autoRefreshEnabled: Boolean,
    autoRefreshInterval: Int,
    onNodeSelected: (String?) -> Unit,
    onModelSelected: (String?) -> Unit,
    onTimeRangeSelected: (MetricsViewModel.TimeRange) -> Unit,
    onAutoRefreshToggled: () -> Unit,
    onAutoRefreshIntervalChanged: (Int) -> Unit,
    onExportMetrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nodeDropdownExpanded by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var timeRangeDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Time Range",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Range Selector
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { timeRangeDropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedTimeRange.displayName)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Time Range"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = timeRangeDropdownExpanded,
                        onDismissRequest = { timeRangeDropdownExpanded = false }
                    ) {
                        MetricsViewModel.TimeRange.values().forEach { timeRange ->
                            DropdownMenuItem(
                                text = { Text(timeRange.displayName) },
                                onClick = {
                                    onTimeRangeSelected(timeRange)
                                    timeRangeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Node Filter
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { nodeDropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                nodes.find { it.id == selectedNode }?.name
                                    ?: "All Nodes"
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Node"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = nodeDropdownExpanded,
                        onDismissRequest = { nodeDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Nodes") },
                            onClick = {
                                onNodeSelected(null)
                                nodeDropdownExpanded = false
                            }
                        )

                        nodes.forEach { node ->
                            DropdownMenuItem(
                                text = { Text(node.name.orEmpty()) },
                                onClick = {
                                    onNodeSelected(node.id)
                                    nodeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Model Filter
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { modelDropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                models.find { it.id == selectedModel }?.name
                                    ?: "All Models"
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Model"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = modelDropdownExpanded,
                        onDismissRequest = { modelDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Models") },
                            onClick = {
                                onModelSelected(null)
                                modelDropdownExpanded = false
                            }
                        )

                        models.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model.name.orEmpty()) },
                                onClick = {
                                    onModelSelected(model.id)
                                    modelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto Refresh and Export
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto Refresh Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Auto Refresh")
                    Switch(
                        checked = autoRefreshEnabled,
                        onCheckedChange = { onAutoRefreshToggled() }
                    )

                    if (autoRefreshEnabled) {
                        Text("${autoRefreshInterval}s")
                    }
                }

                // Export Button
                Button(
                    onClick = onExportMetrics,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Export CSV")
                }
            }
        }
    }
}
