@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.work.Configuration
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.apollographql.apollo3.cache.normalized.sql.ApolloInitializer
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.wear.home.navigation.ConferenceHomeDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowSettings


@RunWith(RobolectricTestRunner::class)
@Config(application = KoinTestApp::class, sdk = [30])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
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
        WorkManagerTestInitHelper.initializeTestWorkManager(get(), config)

        ApolloInitializer().create(get())
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

        ShadowSettings.setAirplaneMode(true)

        navController.navigate(ConferenceHomeDestination.createNavigationRoute("test"))

        rule.waitUntil {
            val tree = rule.onRoot().printToString()
            tree.contains("Settings")
        }
    }
}