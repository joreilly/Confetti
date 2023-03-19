@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.johnoreilly.confetti.AppSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = KoinTestApp::class, sdk = [30])
class AppTest : KoinTest {
    @get:Rule
    val rule = createAndroidComposeRule(MainActivity::class.java)

    val appSettings: AppSettings by inject()

    @After
    fun after() {
        stopKoin()
    }

    @Test
    fun launchHome() = runTest {
        val activity = rule.activity

        val navController = activity.navController

        assertEquals("conference_route/{conference}", navController.currentDestination?.route)

        assertEquals("", appSettings.getConference())
    }
}