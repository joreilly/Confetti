package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AccessibilitySyncOptions
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureIcon
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import dev.johnoreilly.confetti.decompose.DefaultAppComponent

@OptIn(ExperimentalDecomposeApi::class, ExperimentalComposeApi::class)
fun MainViewController(
    component: DefaultAppComponent,
    backDispatcher: BackDispatcher,
) = ComposeUIViewController(
    configure = { accessibilitySyncOptions = AccessibilitySyncOptions.Always(null) }
) {
    PredictiveBackGestureOverlay(
        backDispatcher = backDispatcher,
        backIcon = { progress, _ ->
            PredictiveBackGestureIcon(
                imageVector = Icons.Default.ArrowBackIosNew,
                progress = progress,
            )
        },
        modifier = Modifier.fillMaxSize(),
        endEdgeEnabled = false,
    ) {
        App(component)
    }
}
