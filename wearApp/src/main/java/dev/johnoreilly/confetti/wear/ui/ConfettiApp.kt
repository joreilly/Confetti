package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import dev.johnoreilly.confetti.AppSettings
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
    // Curated conference theme overrides the backend-supplied seedColor +
    // default typography when the active conference id is one we recognise
    // (KotlinConf / AndroidMakers / Droidcon / DevFest). Everyone else keeps
    // whatever the backend sent plus the default Expressive typography.
    val conferenceTheme = conferenceThemeFor(appState?.defaultConference)

    ConfettiTheme(
        seedColor = conferenceTheme?.seedColor ?: appState?.seedColor,
        typography = conferenceTheme?.typography ?: ExpressiveTypography,
    ) {
        AppScaffold(timeText = { TimeText() }) {
            SwipeToDismissBox(
                component.stack,
                onDismissed = { component.navigateUp() },
            ) { configuration ->
                when (val child = configuration.instance) {
                    is Child.Conferences -> ConferencesRoute(child.component)
                    is Child.ConferenceSessions -> SessionsRoute(child.component)
                    is Child.SessionDetails -> SessionDetailsRoute(child.component)
                    is Child.SpeakerDetails -> SpeakerDetailsRoute(child.component)
                    is Child.Settings -> SettingsRoute(child.component)
                    is Child.Loading -> LoadingScreen(component)
                    is Child.Home -> HomeRoute(child.component)
                    is Child.Bookmarks -> BookmarksRoute(child.component)
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
