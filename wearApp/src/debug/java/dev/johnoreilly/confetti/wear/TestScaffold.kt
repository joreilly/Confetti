@file:OptIn(ExperimentalCoilApi::class)

package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.LocalImageLoader
import coil.test.FakeImageLoaderEngine
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.toColor

@Composable
fun TestScaffold(fakeImageLoader: FakeImageLoaderEngine? = null, content: @Composable () -> Unit) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (fakeImageLoader != null) {
                add(fakeImageLoader)
            }
        }
        .build()

    @Suppress("DEPRECATION")
    CompositionLocalProvider(LocalImageLoader provides imageLoader) {
        AppScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            timeText = { TimeText(timeSource = FixedTimeSource) { time() } }
        ) {
            ConfettiTheme(seedColor = null.toColor()) {
                content()
            }
        }
    }
}