package dev.johnoreilly.confetti.wear.app

import androidx.core.net.toUri
import org.junit.Test

class NavigationTest : BaseAppTest() {

    @Test
    fun deeplinks() {
        val activity = rule.activity

        val navController = activity.navController

        navController.navigate("confetti://confetti/signIn".toUri())
        navController.navigate("confetti://confetti/signOut".toUri())
        navController.navigate("confetti://confetti/settings".toUri())
        navController.navigate("confetti://confetti/conferences".toUri())
        navController.navigate("confetti://confetti/conferenceHome/test".toUri())
        navController.navigate("confetti://confetti/sessions/2023-01-01".toUri())
        navController.navigate("confetti://confetti/speaker/test/abc".toUri())
    }
}