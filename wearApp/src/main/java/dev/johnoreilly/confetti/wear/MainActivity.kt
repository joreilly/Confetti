@file:OptIn(KoinInternalApi::class)

package dev.johnoreilly.confetti.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.firebase.FirebaseApp
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.auth.navigation.SignInDestination
import dev.johnoreilly.confetti.wear.conferences.navigation.ConferencesDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import org.koin.android.ext.android.inject
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatformTools

class MainActivity : ComponentActivity() {
    lateinit var navController: NavHostController
    private val analyticsLogger: AnalyticsLogger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberSwipeDismissableNavController()

            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin?
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                ConfettiApp(navController, intent)

                LaunchedEffect(Unit) {
                    navigateFromTileLaunch()
                }

                LaunchedEffect(Unit) {
                    logNavigationEvents()
                }
            }
        }
    }

    private suspend fun logNavigationEvents() {
        if (isFirebaseInstalled) {
            navController.currentBackStackEntryFlow.collect { navEntry ->
                analyticsLogger.logNavigationEvent(navEntry)
            }
        }
    }

    private val isFirebaseInstalled: Boolean
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (ise: IllegalStateException) {
            false
        }

    fun navigateFromTileLaunch() {
        val tileButton = intent.getAndRemoveKey("tile")
        if (tileButton == "session") {
            val conference = intent.getAndRemoveKey("conference")
            val sessionId = intent.getAndRemoveKey("session")

            if (conference != null && sessionId != null) {
                navController.navigate(
                    SessionDetailsDestination.createNavigationRoute(
                        SessionDetailsKey(conference, sessionId)
                    )
                )
            }
        } else if (tileButton == "login") {
            navController.navigate(SignInDestination.route)
        } else if (tileButton == "conferences") {
            navController.navigate(ConferencesDestination.route)
        }
    }
}

private fun Intent.getAndRemoveKey(key: String): String? =
    getStringExtra(key).also {
        removeExtra(key)
    }