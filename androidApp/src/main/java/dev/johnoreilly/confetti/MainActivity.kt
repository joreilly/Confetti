package dev.johnoreilly.confetti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.johnoreilly.confetti.ui.ConfettiApp
import dev.johnoreilly.confetti.ui.ConfettiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConfettiTheme {
                ConfettiApp()
            }
        }
    }
}


