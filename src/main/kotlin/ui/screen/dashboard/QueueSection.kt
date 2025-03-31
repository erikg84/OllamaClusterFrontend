package ui.screen.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domain.model.QueueStatus
import domain.model.TimeSeriesPoint

@Composable
fun QueueSection(
    queueStatus: QueueStatus?,
    responseTimeData: List<TimeSeriesPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Queue header with toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Request Queue",
                    style = MaterialTheme.typography.titleMedium
                )

                queueStatus?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Queue Active",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = it.active == true,
                            onCheckedChange = null, // Handled by ViewModel
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Queue stats
            queueStatus?.let { status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QueueStatItem(
                        label = "Pending",
                        value = status.pending.toString(),
                        color = MaterialTheme.colorScheme.error
                    )
                    QueueStatItem(
                        label = "Processing",
                        value = status.processing.toString(),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    QueueStatItem(
                        label = "Completed",
                        value = status.completed.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Response time chart
            Text(
                text = "Response Time (last hour)",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ResponseTimeChart(
                data = responseTimeData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
fun QueueStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}
