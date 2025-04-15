import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import org.koin.core.context.startKoin
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.LLMClusterTheme
import di.appModule
import org.koin.compose.koinInject
import ui.chatbot.ChatScreen
import viewmodel.InteractViewModel

@Composable
fun App(composeWindow: ComposeWindow) {
    LLMClusterTheme {
        val viewModel = koinInject<InteractViewModel>()
        ChatScreen(viewModel, composeWindow)
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
        val composeWindow = window
        App(composeWindow)
    }
}
