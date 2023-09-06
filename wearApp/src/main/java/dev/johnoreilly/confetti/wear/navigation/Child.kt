package dev.johnoreilly.confetti.wear.navigation

import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.decompose.SessionDetailsComponent
import dev.johnoreilly.confetti.decompose.SpeakerDetailsComponent
import dev.johnoreilly.confetti.wear.auth.FirebaseSignOutComponent
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksComponent
import dev.johnoreilly.confetti.wear.home.HomeComponent
import dev.johnoreilly.confetti.wear.sessions.ConferenceSessionsComponent
import dev.johnoreilly.confetti.wear.settings.SettingsComponent

sealed class Child {
    object Loading : Child()
    class Conferences(val component: ConferencesComponent) : Child()
    class ConferenceSessions(val component: ConferenceSessionsComponent) : Child()

    class SessionDetails(val component: SessionDetailsComponent) : Child()

    class SpeakerDetails(val component: SpeakerDetailsComponent) : Child()

    class Settings(val component: SettingsComponent) : Child()

    object GoogleSignIn : Child()

    class GoogleSignOut(val component: FirebaseSignOutComponent) : Child()

    class Bookmarks(val component: BookmarksComponent) : Child()

    class Home(val component: HomeComponent) : Child()
}