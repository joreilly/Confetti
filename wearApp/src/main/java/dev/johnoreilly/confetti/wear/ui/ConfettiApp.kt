@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissBoxState
import androidx.wear.compose.material.SwipeToDismissKeys
import androidx.wear.compose.material.Text
import com.arkivanov.decompose.Child.Created
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import dev.johnoreilly.confetti.wear.WearAppViewModel
import dev.johnoreilly.confetti.wear.conferences.ConferencesRoute
import dev.johnoreilly.confetti.wear.decompose.DefaultWearAppComponent
import dev.johnoreilly.confetti.wear.decompose.WearAppComponent
import dev.johnoreilly.confetti.wear.sessions.SessionsRoute
import org.koin.androidx.compose.getViewModel

@Composable
fun ConfettiApp(
    component: WearAppComponent,
    viewModel: WearAppViewModel = getViewModel(),
) {
    val appState by viewModel.appState.collectAsStateWithLifecycle()
    val settings = appState?.settings

    if (settings != null) {
        ConfettiTheme(settings.theme) {
            DecomposeNavHost(component) { configuration ->
                when (val child = configuration.instance) {
                    is WearAppComponent.Child.Conferences -> ConferencesRoute(
                        child.component,
                        ScalingLazyColumnDefaults.belowTimeText().create()
                    )

                    is WearAppComponent.Child.ConferenceSessions -> SessionsRoute(
                        child.component,
                        ScalingLazyColumnDefaults.belowTimeText().create()
                    )

                    else -> Text(
                        text = "Configuration\n${
                            configuration.instance.toString().substringAfterLast(".")
                        }"
                    )
                }
            }
        }
    }
}

@Composable
private fun DecomposeNavHost(
    component: WearAppComponent,
    content: @Composable (Created<Any, Any>) -> Unit
) {
    val stack = component.stack.value
    val previous = stack.backStack.lastOrNull()
    val current = stack.active

    val state = SwipeToDismissBoxState()

    SwipeToDismissBox(
        state = state,
        backgroundKey = previous?.configuration ?: SwipeToDismissKeys.Background,
        contentKey = current.configuration,
        hasBackground = previous != null,
        onDismissed = { component.navigateUp() }
    ) { isBackground ->
        val configuration: Created<Any, Any>? = if (isBackground) {
            previous
        } else {
            current
        }

        if (configuration != null) {
            content(configuration)
        }
    }
}
