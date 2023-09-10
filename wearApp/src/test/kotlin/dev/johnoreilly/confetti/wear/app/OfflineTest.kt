package dev.johnoreilly.confetti.wear.app

import dev.johnoreilly.confetti.wear.navigation.Config
import org.junit.Test
import org.robolectric.shadows.ShadowSettings

class OfflineTest : BaseAppTest() {

    @Test
    fun offlineTest() {
        val activity = rule.activity

        val appComponent = activity.appComponent

        ShadowSettings.setAirplaneMode(true)

        appComponent.navigateTo(Config.Home(null, "test"))

        kotlin.test.assertEquals(Config.Home(null, "test"), appComponent.config)
    }
}