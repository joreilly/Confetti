import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.compose.ui.window.ComposeViewport
import com.apollographql.apollo.ApolloClient
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
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

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val lifecycle = LifecycleRegistry(Lifecycle.State.STARTED)

    val appComponent =
        DefaultAppComponent(
            componentContext = DefaultComponentContext(lifecycle),
            onSignOut = {},
            onSignIn = {}
        )


    ComposeViewport(content = {
            MaterialTheme {
                App(appComponent)
            }
        })
}

