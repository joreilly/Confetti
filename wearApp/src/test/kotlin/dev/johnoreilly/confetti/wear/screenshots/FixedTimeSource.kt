package dev.johnoreilly.confetti.wear.screenshots

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.TimeSource

object FixedTimeSource : TimeSource {
    override val currentTime: String
        @Composable get() = "10:10"
}