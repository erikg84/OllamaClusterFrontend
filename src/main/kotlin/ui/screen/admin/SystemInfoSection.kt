package ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domain.model.SystemInfo

@Composable
fun SystemInfoSection(
    systemInfo: SystemInfo?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "System Information",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (systemInfo == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No system information available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // System info table
                InfoRow("API Version", systemInfo.apiVersion.orEmpty())
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                InfoRow("Server Uptime", systemInfo.uptime.orEmpty())
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                InfoRow("CPU Usage", "${systemInfo.cpuUsage}%")
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                InfoRow("Memory Usage", systemInfo.memoryUsage.toString())
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                InfoRow("Disk Usage", systemInfo.diskUsage.toString())
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                InfoRow("Node.js Version", systemInfo.nodeJsVersion.orEmpty())
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                InfoRow("Express Version", systemInfo.expressVersion.orEmpty())
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                InfoRow("Environment", systemInfo.environment.orEmpty())
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}
