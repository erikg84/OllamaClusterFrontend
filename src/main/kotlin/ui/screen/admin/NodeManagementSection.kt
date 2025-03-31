package ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Node

@Composable
fun NodeManagementSection(
    nodes: List<Node>,
    onRestartClick: (String) -> Unit,
    onShutdownClick: (String) -> Unit,
    onUpdateModelsClick: (String) -> Unit,
    isPerformingOperation: Boolean,
    operationMessage: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Node Management",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (nodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No nodes available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    nodes.forEach { node ->
                        NodeControlRow(
                            node = node,
                            onRestartClick = { node.id?.let { onRestartClick(it) } },
                            onShutdownClick = { node.id?.let { onShutdownClick(it) } },
                            onUpdateModelsClick = { node.id?.let { onUpdateModelsClick(it) } },
                            isDisabled = isPerformingOperation
                        )
                    }
                }
            }

            // Operation status message
            if (operationMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = operationMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun NodeControlRow(
    node: Node,
    onRestartClick: () -> Unit,
    onShutdownClick: () -> Unit,
    onUpdateModelsClick: () -> Unit,
    isDisabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Node name and status
        Text(
            text = node.name.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRestartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isDisabled && node.isOnline
            ) {
                Text("Restart")
            }

            Button(
                onClick = onShutdownClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = !isDisabled && node.isOnline
            ) {
                Text("Shutdown")
            }

            Button(
                onClick = onUpdateModelsClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                enabled = !isDisabled && node.isOnline
            ) {
                Text("Update Models")
            }
        }
    }
}
