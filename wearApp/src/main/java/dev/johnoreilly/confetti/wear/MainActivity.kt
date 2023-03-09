@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val repository: ConfettiRepository by inject()
    private val analyticsLogger: AnalyticsLogger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startingConferenceAsync = lifecycleScope.async {
            repository.getConference()
        }

        setContent {
            val navController = rememberSwipeDismissableNavController()

            val startingConference = remember {
                runBlocking {
                    startingConferenceAsync.await()
                }
            }

            ConfettiTheme {
                ConfettiApp(
                    startingConference,
                    navController
                )
            }

            LaunchedEffect(Unit) {
                val conference = intent.getAndRemoveKey("conference")
                val sessionId = intent.getAndRemoveKey("session")

                if (sessionId != null && conference != null) {
                    navController.navigate(
                        SessionDetailsDestination.createNavigationRoute(
                            SessionDetailsKey(conference, sessionId)
                        )
                    )
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