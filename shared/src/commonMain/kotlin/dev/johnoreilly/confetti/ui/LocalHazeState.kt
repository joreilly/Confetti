package dev.johnoreilly.confetti.ui

import androidx.compose.runtime.staticCompositionLocalOf
import dev.chrisbanes.haze.HazeState

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }
val LocalBottomNavigationPadding = compositionLocalOf<Dp> { 0.dp }

/**
 * How collapsed [HomeScaffold]'s top app bar currently is, from 0 (fully expanded) to 1 (fully
 * collapsed) - driven by the same nested-scroll signal the app bar itself hides on. Lets content
 * below the app bar (e.g. a track filter row) collapse away in sync with it, rather than staying
 * pinned while the bar above it hides.
 */
val LocalTopBarCollapsedFraction = compositionLocalOf { 0f }

