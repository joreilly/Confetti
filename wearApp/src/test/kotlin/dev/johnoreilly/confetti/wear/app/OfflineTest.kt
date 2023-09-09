package dev.johnoreilly.confetti.wear.app

import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import dev.johnoreilly.confetti.wear.navigation.Config
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.shadows.ShadowSettings

class OfflineTest : BaseAppTest() {

    @Test
    fun offlineTest() {
        val activity = rule.activity

        val appComponent = activity.appComponent

        rule.waitUntil {
            appComponent.config !is Config.Loading
        }

        assertEquals(Config.Conferences, appComponent.config)

        ShadowSettings.setAirplaneMode(true)

        appComponent.navigateTo(Config.Home(null, "test"))

        rule.waitUntil {
            val tree = rule.onRoot().printToString()
            tree.contains("Settings")
        }
    }
}