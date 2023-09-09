package dev.johnoreilly.confetti.wear.app

import androidx.compose.ui.test.onNodeWithText
import dev.johnoreilly.confetti.wear.navigation.Config
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AppTest : BaseAppTest() {
    @Test
    fun launchHomeWithNoConference() {
        val activity = rule.activity

        val appComponent = activity.appComponent

        rule.waitUntil {
            appComponent.config !is Config.Loading
        }

        assertEquals(Config.Conferences, appComponent.config)

        rule.onNodeWithText("Conferences")
            .assertExists()
    }

    @Test
    fun launchHomeWithConference() {
        runBlocking {
            appSettings.setConference(TestFixtures.kotlinConf2023.id)
        }

        val activity = rule.activity

        val appComponent = activity.appComponent

        rule.waitUntil {
            appComponent.config !is Config.Loading
        }

        assertEquals(Config.Home(null, TestFixtures.kotlinConf2023.id), appComponent.config)

        rule.onNodeWithText("Conference Days")
            .assertExists()
    }
}