package ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import domain.model.Model
import domain.model.Node
import domain.model.NodeStatus

@Composable
fun NodeSection(
    nodes: List<Node>,
    models: List<Model>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            nodes.forEach { node ->
                NodeCard(
                    node = node,
                    modelCount = models.count { it.node == node.id },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NodeCard(
    node: Node,
    modelCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.name.orEmpty(),
                    style = MaterialTheme.typography.titleMedium
                )

                node.status?.let { StatusBadge(status = it) }
            }

            Column {
                Text(
                    text = "Models Loaded",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "$modelCount",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: NodeStatus) {
    val backgroundColor = when (status) {
        NodeStatus.ONLINE -> Color(0xFF4CAF50)
        NodeStatus.OFFLINE -> Color(0xFFE53935)
    }

    val statusText = when (status) {
        NodeStatus.ONLINE -> "Online"
        NodeStatus.OFFLINE -> "Offline"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = statusText,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
