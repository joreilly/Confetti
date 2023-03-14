package dev.johnoreilly.confetti.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
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
    private val repository: ConfettiRepository by inject()
    private val analyticsLogger: AnalyticsLogger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.async {
            repository.getConference()
        }

        setContent {
            val navController = rememberSwipeDismissableNavController()

            ConfettiTheme {
                ConfettiApp(navController)
            }

            LaunchedEffect(Unit) {
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

            LaunchedEffect(Unit) {
                navController.currentBackStackEntryFlow.collect { navEntry ->
                    val conference = repository.getConference()
                    analyticsLogger.logNavigationEvent(conference, navEntry)
                }
            }
        }
    }
}

private fun Intent.getAndRemoveKey(key: String): String? =
    getStringExtra(key).also {
        removeExtra(key)
    }