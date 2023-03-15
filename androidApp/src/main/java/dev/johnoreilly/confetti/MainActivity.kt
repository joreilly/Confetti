@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package dev.johnoreilly.confetti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.adaptive.calculateDisplayFeatures
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.ui.ConfettiApp
import dev.johnoreilly.confetti.ui.ConfettiTheme
import dev.johnoreilly.confetti.ui.component.ConfettiBackground
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {
    private val repository: ConfettiRepository by inject()
    private val analyticsLogger: AnalyticsLogger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()

            val windowSizeClass = calculateWindowSizeClass(this)
            val displayFeatures = calculateDisplayFeatures(this)

            ConfettiTheme {
                ConfettiBackground {
                    ConfettiApp(
                        navController = navController,
                        windowSizeClass = windowSizeClass,
                        displayFeatures = displayFeatures,
                    )
                }
            }

            LaunchedEffect(Unit) {
                navController.currentBackStackEntryFlow.collect { navEntry ->
                    analyticsLogger.logNavigationEvent(repository.getConference(), navEntry)
                }
            }
        }
    }
}
