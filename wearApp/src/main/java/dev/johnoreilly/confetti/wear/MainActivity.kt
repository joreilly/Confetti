package dev.johnoreilly.confetti.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import kotlinx.coroutines.async
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    lateinit var navController: NavHostController
    private val repository: ConfettiRepository by inject()
    private val analyticsLogger: AnalyticsLogger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberSwipeDismissableNavController()

            ConfettiTheme {
                ConfettiApp(navController)
            }

            LaunchedEffect(Unit) {
                navigateFromTileLaunch()
            }

            LaunchedEffect(Unit) {
                logNavigationEvents()
            }
        }
    }

    private suspend fun logNavigationEvents() {
        navController.currentBackStackEntryFlow.collect { navEntry ->
            val conference = repository.getConference()
            analyticsLogger.logNavigationEvent(conference, navEntry)
        }
    }

    fun navigateFromTileLaunch() {
        if (intent.getAndRemoveKey("tile") == "session") {
            val conference = intent.getAndRemoveKey("conference")
            val sessionId = intent.getAndRemoveKey("session")

            if (conference != null && sessionId != null) {
                navController.navigate(
                    SessionDetailsDestination.createNavigationRoute(
                        SessionDetailsKey(conference, sessionId)
                    )
                )
            }
        }
    }
}

private fun Intent.getAndRemoveKey(key: String): String? =
    getStringExtra(key).also {
        removeExtra(key)
    }