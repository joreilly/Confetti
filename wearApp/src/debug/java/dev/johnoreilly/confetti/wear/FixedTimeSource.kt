package dev.johnoreilly.confetti.wear

import androidx.compose.runtime.Composable

object FixedTimeSource : androidx.wear.compose.material3.TimeSource {
    @Composable
    override fun currentTime(): String = "10:10"
}