import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

@Composable
@Preview
fun App() {
    MaterialTheme {
        TextWriterUi()
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Multi-Document Text Editor",
        state = rememberWindowState(height = 800.dp, width = 600.dp)
    ) {
        App()
    }
}
