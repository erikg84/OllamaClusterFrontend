package ui.screen

import navigation.NavigationDestination
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navigation.NavigationHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentDestination by remember { mutableStateOf(NavigationDestination.Dashboard) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LLM Cluster Dashboard") },
                actions = {
                    // Queue status toggle
                    Button(
                        onClick = { /* Toggle queue status */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Queue Active")
                    }

                    // Refresh button
                    Button(
                        onClick = { /* Refresh data */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Refresh")
                    }
                }
            )
        },
        bottomBar = { },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                // Navigation tabs
                TabRow(
                    selectedTabIndex = currentDestination.ordinal,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    NavigationDestination.entries.forEach { destination ->
                        Tab(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            text = { Text(destination.title) },
                            selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }

                // Main content area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    NavigationHost(currentDestination)
                }
            }
        }
    )
}
