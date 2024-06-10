package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
//    primary = Purple200,
//    primaryVariant = Purple700,
//    secondary = Teal200
    primary = DeepPurple50,
    primaryVariant = DeepPurple100,
    secondary = DeepPurple200,

    background = DeepPurple50,
    surface = DeepPurple300,

    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val LightColorPalette = lightColors(
//    primary = Purple500,
//    primaryVariant = Purple700,
//    secondary = Teal200


//    primary = Red50,
//    primaryVariant = Red100,
//    secondary = Red200,
//
//    background = Red50,
//    surface = Red300,
//    onPrimary = Color.Black,
//    onSecondary = Color.Black,
//    onBackground = Color.Black,
//    onSurface = Color.Black,


    primary = BlueLight,
    primaryVariant = BlueLightVariant,
    secondary = BlueDark,
    background = WhiteLight,
    surface = GreenCon,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = GreenBack,

//    primary = DeepPurple50,
//    primaryVariant = DeepPurple100,
//    secondary = DeepPurple200,
//
//    background = DeepPurple50,
//    surface = DeepPurple300,
)

@Composable
fun LambdaCalculatorTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    MaterialTheme(
        colors = colors,
        shapes = Shapes,
        content = content
    )
}