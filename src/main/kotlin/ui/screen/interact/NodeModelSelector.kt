package ui.screen.interact

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = "Node",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { expandedNodeDropdown = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedNode?.name ?: "Select Node",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Node"
                    )
                }
            }

            // Node dropdown menu
            DropdownMenu(
                expanded = expandedNodeDropdown,
                onDismissRequest = { expandedNodeDropdown = false },
                modifier = Modifier.fillMaxWidth(0.95f)
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

        // Model selector
        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = "Model",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (models.isNotEmpty()) {
                        expandedModelDropdown = true
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedModel?.name ?:
                        if (models.isEmpty()) "No models available" else "Select Model",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (models.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Model"
                        )
                    }
                }
            }

            // Model dropdown menu
            DropdownMenu(
                expanded = expandedModelDropdown,
                onDismissRequest = { expandedModelDropdown = false },
                modifier = Modifier.fillMaxWidth(0.95f)
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
