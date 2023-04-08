package dev.johnoreilly.confetti.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.settings.SettingsRoute
import dev.johnoreilly.confetti.ui.ConfettiAppState

private const val base = "settings"

private val settingsPattern = base


internal object SettingsKey{
    val route: String = settingsPattern
}

fun NavGraphBuilder.settingsGraph() {
    composable(
        route = settingsPattern,
        arguments = emptyList()
    ) {
        SettingsRoute()
    }
}
