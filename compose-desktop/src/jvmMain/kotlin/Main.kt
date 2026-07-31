import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.apollographql.apollo.ApolloClient
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.russhwolf.settings.ObservableSettings
import dev.johnoreilly.confetti.decompose.DefaultAppComponent
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.ui.App
import kotlinx.coroutines.flow.collectLatest
import org.koin.dsl.module


private fun mainModule() = module {
    factory {
        ApolloClient.Builder()
            .serverUrl("https://confetti-app.dev/graphql")
    }
}

val koin = initKoin {
    modules(mainModule())
}.koin

fun main() {
    val lifecycle = LifecycleRegistry()

    val appComponent =
        runOnUiThread {
            DefaultAppComponent(
                componentContext = DefaultComponentContext(lifecycle),
                onSignOut = {},
                onSignIn = {}
            )
        }

    val settings = koin.get<ObservableSettings>()

    application {
        val windowState = rememberWindowState(
            width = settings.getFloat(KEY_WINDOW_WIDTH, 600f).dp,
            height = settings.getFloat(KEY_WINDOW_HEIGHT, 800f).dp,
            position = if (settings.hasKey(KEY_WINDOW_X) && settings.hasKey(KEY_WINDOW_Y)) {
                WindowPosition(
                    x = settings.getFloat(KEY_WINDOW_X, 0f).dp,
                    y = settings.getFloat(KEY_WINDOW_Y, 0f).dp,
                )
            } else {
                WindowPosition.PlatformDefault
            },
        )

        // Persist size/position whenever the user resizes or moves the window,
        // so it reopens where it was left.
        LaunchedEffect(windowState) {
            snapshotFlow { windowState.size to windowState.position }
                .collectLatest { (size, position) ->
                    if (size.isSpecified) {
                        settings.putFloat(KEY_WINDOW_WIDTH, size.width.value)
                        settings.putFloat(KEY_WINDOW_HEIGHT, size.height.value)
                    }
                    if (position.isSpecified) {
                        settings.putFloat(KEY_WINDOW_X, position.x.value)
                        settings.putFloat(KEY_WINDOW_Y, position.y.value)
                    }
                }
        }

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Confetti"
        ) {
            LifecycleController(
                lifecycleRegistry = lifecycle,
                windowState = windowState,
                windowInfo = LocalWindowInfo.current,
            )

            MaterialTheme {
                App(appComponent)
            }
        }
    }
}

private const val KEY_WINDOW_WIDTH = "window_width"
private const val KEY_WINDOW_HEIGHT = "window_height"
private const val KEY_WINDOW_X = "window_x"
private const val KEY_WINDOW_Y = "window_y"


