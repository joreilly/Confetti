package dev.johnoreilly.confetti.wear.app

import androidx.compose.ui.test.onNodeWithText
import dev.johnoreilly.confetti.wear.navigation.Config
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.Description

class AppTest : BaseAppTest() {

    override suspend fun configure(description: Description) {
        if (description.methodName.contains("WithConference")) {
            appSettings.setConference(TestFixtures.kotlinConf2023.id)
        }
    }

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