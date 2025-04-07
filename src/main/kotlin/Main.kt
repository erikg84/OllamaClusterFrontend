import androidx.compose.runtime.Composable
import org.koin.core.context.startKoin
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.LLMClusterTheme
import di.appModule
import org.koin.compose.koinInject
import ui.screen.ChatScreen
import ui.screen.MainScreen
import viewmodel.InteractViewModel

@Composable
fun App() {
    LLMClusterTheme {
        val viewModel = koinInject<InteractViewModel>()
        ChatScreen(viewModel)
    }
}

fun main() = application {
    startKoin {
        modules(appModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "LLM Cluster Dashboard"
    ) {
        App()
    }
}
