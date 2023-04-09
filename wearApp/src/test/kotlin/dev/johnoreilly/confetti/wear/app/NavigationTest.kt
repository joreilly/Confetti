package dev.johnoreilly.confetti.wear.app

import androidx.core.net.toUri
import dev.johnoreilly.confetti.wear.conferences.navigation.ConferencesDestination
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.startup.navigation.StartHomeDestination
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

class NavigationTest : BaseAppTest() {
    @Test
    @Ignore("multiple fails")
    fun launchWithoutConference() {
        val activity = rule.activity

        val navController = activity.navController

        assertEquals(StartHomeDestination.route, navController.currentDestination?.route)

        rule.waitUntil {
            navController.currentDestination?.route == ConferencesDestination.route
        }
    }

    @Test
//    @Ignore("multiple fails")
    fun launchWithConference() {
        runBlocking {
            appSettings.setConference(TestFixtures.kotlinConf2023.id)
        }

        val activity = rule.activity

        val navController = activity.navController

        assertEquals(StartHomeDestination.route, navController.currentDestination?.route)

        rule.waitUntil {
            navController.currentDestination?.route == StartHomeDestination.route
        }
    }

    @Test
    @Ignore("multiple fails")
    fun deeplinks() {
        val activity = rule.activity

        val navController = activity.navController

        navController.navigate("confetti://confetti/signInPrompt".toUri())
        navController.navigate("confetti://confetti/signIn".toUri())
        navController.navigate("confetti://confetti/signOut".toUri())
        navController.navigate("confetti://confetti/settings".toUri())
        navController.navigate("confetti://confetti/conferences".toUri())
        navController.navigate("confetti://confetti/conferenceHome/test".toUri())
        navController.navigate("confetti://confetti/sessions/2023-01-01".toUri())
        navController.navigate("confetti://confetti/speaker/test/abc".toUri())
    }
}