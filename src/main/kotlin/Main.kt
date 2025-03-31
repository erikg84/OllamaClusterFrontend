import androidx.compose.runtime.Composable
import org.koin.core.context.startKoin
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.LLMClusterTheme
import di.appModule
import ui.screen.MainScreen

@Composable
fun App() {
    LLMClusterTheme {
        MainScreen()
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
