@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.wear.conferences.ConferencesRoute
import dev.johnoreilly.confetti.wear.home.navigation.HomeDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val repository: ConfettiRepository by inject()
    private val analyticsLogger: AnalyticsLogger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberSwipeDismissableNavController()

            ConfettiTheme {
                ConfettiApp(navController)
            }

            LaunchedEffect(Unit) {
                val conference = intent.getAndRemoveKey("conference")
                val sessionId = intent.getAndRemoveKey("session")

                if (conference != null && sessionId != null) {
                    navController.navigate(
                        SessionDetailsDestination.createNavigationRoute(
                            SessionDetailsKey(conference, sessionId)
                        )
                    )
                } else {
                    if (intent.action == Intent.ACTION_MAIN) {
                        val selectedConference = repository.getConference()

                        if (selectedConference != null) {
                            navController.navigate(
                                HomeDestination.createNavigationRoute(selectedConference)
                            )
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                navController.currentBackStackEntryFlow.collect { navEntry ->
                    analyticsLogger.logNavigationEvent(navEntry)
                }
            }
        }
    }
}

private fun Intent.getAndRemoveKey(key: String): String? =
    getStringExtra(key).also {
        removeExtra(key)
    }