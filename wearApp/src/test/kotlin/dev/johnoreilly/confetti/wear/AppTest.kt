@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import dev.johnoreilly.confetti.wear.home.navigation.ConferenceHomeDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.shadows.ShadowSettings

class AppTest : BaseAppTest() {
    @Test
    fun launchHome() = runTest {
        val activity = rule.activity

        val navController = activity.navController

        assertEquals("conference_route/{conference}", navController.currentDestination?.route)

        assertEquals("", appSettings.getConference())
    }

    @Test
    fun offlineTest() = runTest {
        val activity = rule.activity

        val navController = activity.navController

        assertEquals("conference_route/{conference}", navController.currentDestination?.route)

        assertEquals("", appSettings.getConference())

        ShadowSettings.setAirplaneMode(true)

        navController.navigate(ConferenceHomeDestination.createNavigationRoute("test"))

        rule.waitUntil {
            val tree = rule.onRoot().printToString()
            tree.contains("Settings")
        }
    }
}