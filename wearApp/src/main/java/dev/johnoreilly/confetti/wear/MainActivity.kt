@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.wear.conferences.ConferencesRoute
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val repository: ConfettiRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var showLandingScreen by remember {
                mutableStateOf(repository.getConference().isEmpty())
            }

            ConfettiTheme {
                if (showLandingScreen) {
                    ConferencesRoute(
                        columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
                        navigateToConference = {
                            showLandingScreen = false
                        }
                    )
                } else {
                    ConfettiApp()
                }
            }
        }
    }
}