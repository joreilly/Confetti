package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.wear.WearAppViewModel
import dev.johnoreilly.confetti.wear.auth.FirebaseSignInScreen
import dev.johnoreilly.confetti.wear.auth.FirebaseSignOutScreen
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksRoute
import dev.johnoreilly.confetti.wear.conferences.ConferencesRoute
import dev.johnoreilly.confetti.wear.decompose.SwipeToDismissBox
import dev.johnoreilly.confetti.wear.home.HomeRoute
import dev.johnoreilly.confetti.wear.navigation.Child
import dev.johnoreilly.confetti.wear.navigation.WearAppComponent
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsRoute
import dev.johnoreilly.confetti.wear.sessions.SessionsRoute
import dev.johnoreilly.confetti.wear.settings.SettingsRoute
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsRoute
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
            SwipeToDismissBox(
                component.stack,
                onDismissed = { component.navigateUp() }
            ) { configuration ->
                when (val child = configuration.instance) {
                    is Child.Conferences -> ConferencesRoute(
                        child.component,
                        createScalingLazyColumnState()
                    )

                    is Child.ConferenceSessions -> SessionsRoute(
                        child.component,
                        createScalingLazyColumnState()
                    )

                    is Child.SessionDetails -> SessionDetailsRoute(
                        child.component,
                        createScalingLazyColumnState()
                    )

                    is Child.SpeakerDetails -> SpeakerDetailsRoute(
                        child.component,
                        createScalingLazyColumnState()
                    )

                    is Child.Settings -> SettingsRoute(
                        child.component,
                        createScalingLazyColumnState()
                    )

                    is Child.Loading -> {
                        // TODO Loading?
                    }

                    is Child.GoogleSignIn -> {
                        FirebaseSignInScreen(child.component)
                    }

                    is Child.GoogleSignOut ->
                        FirebaseSignOutScreen(child.component)

                    is Child.Home -> HomeRoute(
                        child.component,
                        createScalingLazyColumnState()
                    )

                    is Child.Bookmarks -> BookmarksRoute(
                        child.component,
                        createScalingLazyColumnState()
                    )
                }
            }
        }
    }
}
