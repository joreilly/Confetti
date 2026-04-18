package dev.johnoreilly.confetti.wear.preview

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.TimeSource

object FixedTimeSource : TimeSource {
    @Composable
    override fun currentTime(): String = "10:10"
}
