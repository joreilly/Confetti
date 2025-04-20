import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.apollographql.apollo.ApolloClient
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.johnoreilly.confetti.decompose.DefaultAppComponent
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.ui.App
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

    application {
        val windowState = rememberWindowState(
            width = 600.dp,
            height = 800.dp
        )

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


