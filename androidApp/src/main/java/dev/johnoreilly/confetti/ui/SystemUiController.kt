package dev.johnoreilly.confetti.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SystemBarsColorEffect(
    systemUiController: SystemUiController = rememberSystemUiController(),
    color: Color = Color.Transparent,
    isSystemInDarkTheme: Boolean,
) {
    val darkIcons = !isSystemInDarkTheme
    DisposableEffect(systemUiController, darkIcons) {
        systemUiController.setSystemBarsColor(
            color = color,
            darkIcons = darkIcons,
        )
        onDispose {}
    }
}
