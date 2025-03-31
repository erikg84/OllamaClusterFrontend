package navigation

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import ui.screen.admin.AdminScreen
import ui.screen.dashboard.DashboardScreen
import ui.screen.interact.InteractScreen
import ui.screen.metrics.MetricsScreen
import viewmodel.AdminViewModel
import viewmodel.DashboardViewModel
import viewmodel.InteractViewModel
import viewmodel.MetricsViewModel

/**
 * Navigation destinations for the application
 */
enum class NavigationDestination(val title: String) {
    Dashboard("Dashboard"),
    Interact("Interact"),
    Metrics("Metrics"),
    Admin("Admin")
}

/**
 * Navigation host that displays the appropriate screen based on the current destination
 */
@Composable
fun NavigationHost(currentDestination: NavigationDestination) {
    when (currentDestination) {
        NavigationDestination.Dashboard -> {
            val viewModel = koinInject<DashboardViewModel>()
            DashboardScreen(viewModel)
        }
        NavigationDestination.Interact -> {
            val viewModel = koinInject<InteractViewModel>()
            InteractScreen(viewModel)
        }
        NavigationDestination.Metrics -> {
            val viewModel = koinInject<MetricsViewModel>()
            MetricsScreen(viewModel)
        }
        NavigationDestination.Admin -> {
            val viewModel = koinInject<AdminViewModel>()
            AdminScreen(viewModel)
        }
    }
}
