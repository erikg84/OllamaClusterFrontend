package ui.screen.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    // Collect all state at the top level
    val isLoading = viewModel.isLoading
    val errors by viewModel.errors.collectAsState(initial = null)
    val nodes by viewModel.nodes.collectAsState()
    val models by viewModel.models.collectAsState()
    val clusterStatus by viewModel.clusterStatus.collectAsState()
    val queueStatus by viewModel.queueStatus.collectAsState()
    val responseTimeData by viewModel.responseTimeData.collectAsState()

    // Refresh dashboard data when the screen is shown
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // Loading state
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Error state
    errors?.let { errorMessage ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    // Dashboard content
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Node status section
        NodeSection(
            nodes = nodes,
            models = models
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Available models section (left)
            ModelsSection(
                models = models,
                modifier = Modifier.weight(1f)
            )

            // Queue status section (right)
            QueueSection(
                queueStatus = queueStatus,
                responseTimeData = responseTimeData,
                modifier = Modifier.weight(1f)
            )
        }

        // LLM Interaction section at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "LLM Interaction",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Note: In a real app, we would include a simple interaction UI here
            // but it's simplified in the dashboard view
        }
    }
}
