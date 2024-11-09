package dev.johnoreilly.confetti.wear.app

import android.content.Intent
import androidx.core.net.toUri
import dev.johnoreilly.confetti.wear.navigation.Config
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.junit.Test
import kotlin.test.assertEquals

class NavigationTest : BaseAppTest() {

    @Test
    fun deeplinks() {
        val activity = rule.activity

        val appComponent = activity.appComponent

        rule.waitUntil {
            appComponent.config !is Config.Loading
        }

        appComponent.handleDeeplink("confetti://confetti/signIn".toDeepLink())
        assertEquals(Config.GoogleSignIn, appComponent.config)

        appComponent.handleDeeplink("confetti://confetti/signOut".toDeepLink())
        assertEquals(Config.GoogleSignOut, appComponent.config)

        appComponent.handleDeeplink("confetti://confetti/settings".toDeepLink())
        assertEquals(Config.Settings, appComponent.config)

        appComponent.handleDeeplink("confetti://confetti/conferences".toDeepLink())
        assertEquals(Config.Conferences, appComponent.config)

        appComponent.handleDeeplink("confetti://confetti/home/test".toDeepLink())
        assertEquals(Config.Home(null, "test"), appComponent.config)

        appComponent.handleDeeplink("confetti://confetti/sessions/test/2023-01-01".toDeepLink())
        assertEquals(Config.ConferenceSessions(null, "test", date = LocalDate.Formats.ISO.parse("2023-01-01")), appComponent.config)

        appComponent.handleDeeplink("confetti://confetti/session/test/session1".toDeepLink())
        assertEquals(Config.SessionDetails(null, "test", "session1"), appComponent.config)

        appComponent.handleDeeplink("confetti://confetti/speaker/test/speaker1".toDeepLink())
        assertEquals(Config.SpeakerDetails(null, "test", "speaker1"), appComponent.config)

        appComponent.handleDeeplink("confetti://confetti/bookmarks/test".toDeepLink())
        assertEquals(Config.Bookmarks(null, "test"), appComponent.config)
    }
}

private fun String.toDeepLink(): Intent = Intent(Intent.ACTION_VIEW, this.toUri())
