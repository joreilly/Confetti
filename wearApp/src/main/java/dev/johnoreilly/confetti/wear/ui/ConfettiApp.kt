@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import dev.johnoreilly.confetti.wear.WearAppViewModel
import dev.johnoreilly.confetti.wear.conferences.ConferencesRoute
import dev.johnoreilly.confetti.wear.decompose.DecomposeNavHost
import dev.johnoreilly.confetti.wear.decompose.WearAppComponent
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsRoute
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

                    is WearAppComponent.Child.SessionDetails -> SessionDetailsRoute(
                        child.component,
                        ScalingLazyColumnDefaults.belowTimeText().create()
                    )

                    else -> Text(
                        text = "Configuration\n${
                            configuration.instance.toString().substringAfterLast(".")
                        }",
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}
