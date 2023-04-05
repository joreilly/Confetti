@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear.app

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

@Ignore("Disable while multiple app tests cause failures")
class AppTest : BaseAppTest() {

    @Test
    fun launchHome() {
        val activity = rule.activity

        val navController = activity.navController

        assertEquals("conference_route/{conference}", navController.currentDestination?.route)

        assertEquals("", getConference())
    }

    private fun getConference(): String {
        return runBlocking { appSettings.getConference() }
    }
}