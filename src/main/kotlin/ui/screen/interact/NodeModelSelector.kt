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
import androidx.compose.ui.graphics.Color
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
            OutlinedTextField(
                value = selectedNode?.name ?: "MAC_STUDIO",
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Node"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedNodeDropdown = true },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                )
            )

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

        // Model selector
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = selectedModel?.name ?: "llama3.2:latest",
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Model"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (models.isNotEmpty()) {
                            expandedModelDropdown = true
                        }
                    },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                ),
                enabled = models.isNotEmpty()
            )

            // Model dropdown menu
            DropdownMenu(
                expanded = expandedModelDropdown,
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