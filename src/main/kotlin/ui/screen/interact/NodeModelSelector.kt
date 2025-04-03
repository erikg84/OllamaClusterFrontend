package ui.screen.interact

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Model
import domain.model.Node

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeModelSelector(
    nodes: List<Node>,
    models: List<Model>,
    selectedNode: Node?,
    selectedModel: Model?,
    onNodeSelected: (Node) -> Unit,
    onModelSelected: (Model) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedNodeDropdown by remember { mutableStateOf(false) }
    var expandedModelDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Node selector
        Column(modifier = Modifier.weight(1f)) {
            Text("Node", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                // The clickable area is the entire surface
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedNodeDropdown = true },
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedNode?.name ?: "MAC_STUDIO",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = if (expandedNodeDropdown)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Node"
                        )
                    }
                }

                // Node dropdown menu
                DropdownMenu(
                    expanded = expandedNodeDropdown,
                    onDismissRequest = { expandedNodeDropdown = false },
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    nodes.forEach { node ->
                        DropdownMenuItem(
                            text = { Text(node.name.orEmpty()) },
                            onClick = {
                                onNodeSelected(node)
                                expandedNodeDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Model selector
        Column(modifier = Modifier.weight(1f)) {
            Text("Model", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = models.isNotEmpty()) {
                            if (models.isNotEmpty()) {
                                expandedModelDropdown = true
                            }
                        },
                    shape = MaterialTheme.shapes.small,
                    color = if (models.isEmpty())
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedModel?.name ?: "llama3.2:latest",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (models.isEmpty())
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (expandedModelDropdown)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Model",
                            tint = if (models.isEmpty())
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Model dropdown menu
                DropdownMenu(
                    expanded = expandedModelDropdown && models.isNotEmpty(),
                    onDismissRequest = { expandedModelDropdown = false },
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model.name.orEmpty()) },
                            onClick = {
                                onModelSelected(model)
                                expandedModelDropdown = false
                            }
                        )
                    }
                }
            }
        }
    }
}
