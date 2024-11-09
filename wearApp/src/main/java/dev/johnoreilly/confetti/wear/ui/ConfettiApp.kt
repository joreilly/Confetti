package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.TimeText
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
import dev.johnoreilly.confetti.wear.proto.NetworkDetail
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsRoute
import dev.johnoreilly.confetti.wear.sessions.SessionsRoute
import dev.johnoreilly.confetti.wear.settings.SettingsRoute
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsRoute

@Composable
fun ConfettiApp(
    component: WearAppComponent
) {
    val appState by component.appState.collectAsStateWithLifecycle()

    ConfettiTheme(seedColor = appState?.seedColor) {
        SwipeToDismissBox(
            component.stack,
            onDismissed = { component.navigateUp() },
        ) { configuration ->
            when (val child = configuration.instance) {
                is Child.Conferences -> ConferencesRoute(
                    child.component
                )

                is Child.ConferenceSessions -> SessionsRoute(
                    child.component
                )

                is Child.SessionDetails -> SessionDetailsRoute(
                    child.component
                )

                is Child.SpeakerDetails -> SpeakerDetailsRoute(
                    child.component
                )

                is Child.Settings -> SettingsRoute(
                    child.component
                )

                is Child.Loading -> {
                    LoadingScreen(
                        component
                    )
                }

                is Child.GoogleSignIn -> {
                    FirebaseSignInScreen(child.component)
                }

                is Child.GoogleSignOut -> FirebaseSignOutScreen(child.component)

                is Child.Home -> HomeRoute(
                    child.component
                )

                is Child.Bookmarks -> BookmarksRoute(
                    child.component
                )
            }
        }
    }
}

@Composable
private fun NetworkTimeText(component: WearAppComponent, showNetworks: NetworkDetail) {
    val networkState by component.networkState.collectAsStateWithLifecycle()
    val enabled =
        showNetworks == NetworkDetail.NETWORK_DETAIL_NETWORKS_AND_DATA || showNetworks == NetworkDetail.NETWORK_DETAIL_NETWORKS

    DataUsageTimeText(
        showData = enabled,
        networkStatus = networkState.networks,
        networkUsage = if (showNetworks == NetworkDetail.NETWORK_DETAIL_NETWORKS_AND_DATA) {
            networkState.dataUsage
        } else {
            null
        }
    )
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
