package dev.johnoreilly.confetti.wear.app

import android.content.Intent
import androidx.core.net.toUri
import dev.johnoreilly.confetti.wear.navigation.Config
import org.junit.Test

class NavigationTest : BaseAppTest() {

    @Test
    fun deeplinks() {
        val activity = rule.activity

        val appComponent = activity.appComponent

        rule.waitUntil {
            appComponent.config !is Config.Loading
        }

        appComponent.handleDeeplink("confetti://confetti/signIn".toDeepLink())
        appComponent.handleDeeplink("confetti://confetti/signOut".toDeepLink())
        appComponent.handleDeeplink("confetti://confetti/settings".toDeepLink())
        appComponent.handleDeeplink("confetti://confetti/conferences".toDeepLink())
        appComponent.handleDeeplink("confetti://confetti/conferenceHome/test".toDeepLink())
        appComponent.handleDeeplink("confetti://confetti/sessions/test/2023-01-01".toDeepLink())
        appComponent.handleDeeplink("confetti://confetti/session/test/session1".toDeepLink())
        appComponent.handleDeeplink("confetti://confetti/speaker/test/speaker1".toDeepLink())
    }
}

private fun String.toDeepLink(): Intent = Intent(Intent.ACTION_VIEW, this.toUri())
