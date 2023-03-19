@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.work.testing.WorkManagerTestInitHelper
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.wear.home.navigation.ConferenceHomeDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.bouncycastle.crypto.params.Blake3Parameters.context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings


@RunWith(RobolectricTestRunner::class)
@Config(application = KoinTestApp::class, sdk = [30])
class AppTest : KoinTest {
    @get:Rule
    val rule = createAndroidComposeRule(MainActivity::class.java)

    val appSettings: AppSettings by inject()

    @Before
    fun setUp() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context, config
        )
    }

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

    @Test
    fun offlineTest() = runTest {
        val activity = rule.activity

        val navController = activity.navController

        assertEquals("conference_route/{conference}", navController.currentDestination?.route)

        assertEquals("", appSettings.getConference())

        rule.awaitIdle()

        ShadowSettings.setAirplaneMode(true)

        navController.navigate(ConferenceHomeDestination.createNavigationRoute("test"))

        rule.awaitIdle()
    }
}