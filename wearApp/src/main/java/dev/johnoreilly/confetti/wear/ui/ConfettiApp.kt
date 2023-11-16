package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.TimeText
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.networks.ui.DataUsageTimeText
import dev.johnoreilly.confetti.AppSettings
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

@Composable
fun ConfettiApp(
    component: WearAppComponent
) {
    val appState by component.appState.collectAsStateWithLifecycle()
    val settings = appState?.settings

    if (settings != null) {
        ConfettiTheme(settings.theme) {
            SwipeToDismissBox(
                component.stack,
                onDismissed = { component.navigateUp() },
                timeText = {
                    if (settings.showData) {
                        val networkState by component.networkState.collectAsStateWithLifecycle()
                        DataUsageTimeText(
                            showData = true,
                            networkStatus = networkState.networks,
                            networkUsage = networkState.dataUsage
                        )
                    } else {
                        TimeText()
                    }
                },
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
                        createScalingLazyColumnState(
                            factory = ScalingLazyColumnDefaults.responsive(firstItemIsFullWidth = false)
                        )
                    )

                    is Child.Settings -> SettingsRoute(
                        child.component,
                        createScalingLazyColumnState()
                    )

                    is Child.Loading -> {
                        LoadingScreen(
                            component
                        )
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

@Composable
fun LoadingScreen(component: WearAppComponent) {
    LaunchedEffect(Unit) {
        val conference = component.waitForConference()

        if (conference == AppSettings.CONFERENCE_NOT_SET) {
            component.showConferences()
        } else {
            component.showConference(conference = conference)
        }
    }
}
