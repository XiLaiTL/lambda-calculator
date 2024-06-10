import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.HomeTabBar
import ui.theme.LambdaCalculatorTheme

@Composable
@Preview
fun App() {
    LambdaCalculatorTheme{
        HomeTabBar()
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Lambda Calculator",
        icon =  BitmapPainter(useResource("icon/icon.png",::loadImageBitmap))
    ) {
        App()
    }
}


