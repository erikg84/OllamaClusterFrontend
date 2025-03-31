package ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import domain.model.LogEntry
import domain.model.LogLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsSection(
    logs: List<LogEntry>,
    selectedLogLevel: LogLevel?,
    onLogLevelSelected: (LogLevel?) -> Unit,
    onClearLogs: () -> Unit,
    onExportLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var logLevelDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System Logs",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Log Level filter
                    Box {
                        OutlinedCard(
                            onClick = { logLevelDropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = selectedLogLevel?.name ?: "All Levels",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Log Level"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = logLevelDropdownExpanded,
                            onDismissRequest = { logLevelDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Levels") },
                                onClick = {
                                    onLogLevelSelected(null)
                                    logLevelDropdownExpanded = false
                                }
                            )

                            LogLevel.values().forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level.name) },
                                    onClick = {
                                        onLogLevelSelected(level)
                                        logLevelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Export button
                    Button(
                        onClick = onExportLogs,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Export")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export Logs"
                        )
                    }

                    // Clear button
                    Button(
                        onClick = onClearLogs,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Clear")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Logs"
                        )
                    }
                }
            }

            // Logs display
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No logs available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(
                            logs.filter {
                                selectedLogLevel == null || it.level == selectedLogLevel
                            }
                        ) { log ->
                            LogEntryRow(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogEntryRow(log: LogEntry) {
    val levelColor = when (log.level) {
        LogLevel.INFO -> Color(0xFF2196F3)  // Blue
        LogLevel.WARN -> Color(0xFFFFA000)  // Amber
        LogLevel.ERROR -> Color(0xFFE53935) // Red
        LogLevel.DEBUG -> Color(0xFF4CAF50) // Green
        else -> Color(0xFF9E9E9E) // Grey
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timestamp
        Text(
            text = log.timestamp.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(160.dp)
        )

        // Log level
        Surface(
            color = levelColor,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.width(60.dp)
        ) {
            Text(
                text = log.level?.name.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Source
        Text(
            text = log.source.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(120.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Message
        Text(
            text = log.message.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
