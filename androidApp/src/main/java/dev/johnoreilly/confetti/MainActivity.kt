@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package dev.johnoreilly.confetti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.google.accompanist.adaptive.calculateDisplayFeatures
import dev.johnoreilly.confetti.conferences.ConferencesRoute
import dev.johnoreilly.confetti.ui.ConfettiApp
import dev.johnoreilly.confetti.ui.ConfettiTheme
import dev.johnoreilly.confetti.ui.component.ConfettiBackground
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {
    private val repository: ConfettiRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val displayFeatures = calculateDisplayFeatures(this)

            var showLandingScreen by remember {
                mutableStateOf(repository.getConference().isEmpty())
            }

            if (showLandingScreen) {
                ConfettiTheme {
                    ConfettiBackground {
                        ConferencesRoute(navigateToConference = { _ ->
                            showLandingScreen = false
                        })
                    }
                }
            } else {
                ConfettiApp(windowSizeClass, displayFeatures)
            }
        }
    }
}
