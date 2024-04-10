import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.apollographql.apollo3.ApolloClient
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.johnoreilly.confetti.decompose.AppComponent
import dev.johnoreilly.confetti.decompose.DefaultAppComponent
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.ui.ConferenceListView
import dev.johnoreilly.confetti.ui.LoadingView
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

@OptIn(ExperimentalDecomposeApi::class)
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
        val windowState = rememberWindowState()

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
                MainLayout(appComponent)
            }
        }
    }
}


@Composable
fun MainLayout(component: DefaultAppComponent) {
    Children(
        stack = component.stack
    ) {
        when (val child = it.instance) {
            is AppComponent.Child.Loading -> LoadingView()
            is AppComponent.Child.Conferences -> ConferenceListView(child.component)
            is AppComponent.Child.Conference -> ConferenceView(child.component)
        }
    }
}
