package ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodel.AdminViewModel

@Composable
fun AdminScreen(viewModel: AdminViewModel) {
    // Loading state
    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Error state
    val errors by viewModel.errors.collectAsState(initial = null)
    errors?.let { errorMessage ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    // Admin screen content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top row: System Info and Resource Monitoring
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // System Info (left)
            SystemInfoSection(
                systemInfo = viewModel.systemInfo.collectAsState().value,
                modifier = Modifier.weight(1f)
            )

            // Resource Monitoring (right)
            ResourceMonitoringSection(
                resourceTrends = viewModel.resourceTrends,
                modifier = Modifier.weight(1f)
            )
        }

        // Middle row: Node Management
        NodeManagementSection(
            nodes = viewModel.nodes.collectAsState().value,
            onRestartClick = { viewModel.restartNode(it) },
            onShutdownClick = { viewModel.shutdownNode(it) },
            onUpdateModelsClick = { viewModel.updateModels(it) },
            isPerformingOperation = viewModel.isPerformingOperation.collectAsState().value,
            operationMessage = viewModel.operationMessage.collectAsState().value,
            modifier = Modifier.fillMaxWidth()
        )

        // System Operations row
        SystemOperationsSection(
            onResetStatistics = { viewModel.resetStats() },
            onPurgeQueue = { viewModel.purgeQueue() },
            onResetConnections = { viewModel.resetConnections() },
            isPerformingOperation = viewModel.isPerformingOperation.collectAsState().value,
            modifier = Modifier.fillMaxWidth()
        )

        // Logs section
        LogsSection(
            logs = viewModel.systemLogs,
            selectedLogLevel = viewModel.logLevelFilter.collectAsState().value,
            onLogLevelSelected = { viewModel.setLogLevelFilter(it) },
            onClearLogs = { viewModel.clearLogs() },
            onExportLogs = { /* Handle export logs */ },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
