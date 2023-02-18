@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val repository: ConfettiRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberSwipeDismissableNavController()

            ConfettiTheme {
                ConfettiApp(navController)
            }

        }
    }
}

private fun Intent.getAndRemoveKey(key: String): String? =
    getStringExtra(key).also {
        removeExtra(key)
    }