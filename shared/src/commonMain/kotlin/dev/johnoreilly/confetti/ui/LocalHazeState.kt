package dev.johnoreilly.confetti.ui

import androidx.compose.runtime.staticCompositionLocalOf
import dev.chrisbanes.haze.HazeState

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }
val LocalBottomNavigationPadding = compositionLocalOf<Dp> { 0.dp }

