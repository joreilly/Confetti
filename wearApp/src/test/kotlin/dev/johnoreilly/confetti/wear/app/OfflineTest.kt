package dev.johnoreilly.confetti.wear.app

import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import dev.johnoreilly.confetti.wear.home.navigation.ConferenceHomeDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.shadows.ShadowSettings

class OfflineTest : BaseAppTest() {

    @Test
    fun offlineTest() {
        val activity = rule.activity

        val navController = activity.navController

        assertEquals("start_route/{conference}", navController.currentDestination?.route)

        ShadowSettings.setAirplaneMode(true)

        navController.navigate(ConferenceHomeDestination.createNavigationRoute("test"))

        rule.waitUntil {
            val tree = rule.onRoot().printToString()
            tree.contains("Settings")
        }
    }
}