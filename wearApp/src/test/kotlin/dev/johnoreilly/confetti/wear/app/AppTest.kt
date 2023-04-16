package dev.johnoreilly.confetti.wear.app

import androidx.compose.ui.test.onNodeWithText
import dev.johnoreilly.confetti.wear.conferences.navigation.ConferencesDestination
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.startup.navigation.StartHomeDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AppTest : BaseAppTest() {

    @Test
    fun launchHomeWithNoConference() {
        val activity = rule.activity

        val navController = activity.navController

        rule.waitUntil {
            navController.currentDestination?.route != null
        }

        assertEquals(StartHomeDestination.route, navController.currentDestination?.route)

        // We got navigated to the conferences screen to select a screen
        rule.onNodeWithText("Conferences")
            .assertExists()

        assertEquals(ConferencesDestination.route, navController.currentDestination?.route)
    }

    @Test
    fun launchHomeWithConference() {
        runBlocking {
            appSettings.setConference(TestFixtures.kotlinConf2023.id)
        }

        val activity = rule.activity

        val navController = activity.navController

        rule.waitUntil {
            navController.currentDestination?.route != null
        }

        assertEquals(StartHomeDestination.route, navController.currentDestination?.route)

        rule.onNodeWithText("Conference Days")
            .assertExists()

        assertEquals(StartHomeDestination.route, navController.currentDestination?.route)
    }
}