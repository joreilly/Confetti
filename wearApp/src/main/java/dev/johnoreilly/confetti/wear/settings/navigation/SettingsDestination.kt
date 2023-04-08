@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import androidx.wear.compose.navigation.composable
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.settings.SettingsRoute

object SettingsDestination : ConfettiNavigationDestination {
    override val route = "settings_route"
    override val destination = "settings_destination"
}

fun NavGraphBuilder.settingsGraph(
    onSwitchConferenceSelected: () -> Unit,
    navigateToGoogleSignIn: () -> Unit,
    navigateToGoogleSignOut: () -> Unit,
) {
    composable(
        route = SettingsDestination.route,
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "confetti://confetti/settings"
            }
        )
    ) {
        SettingsRoute(
            ScalingLazyColumnDefaults.belowTimeText().create(),
            onSwitchConferenceSelected,
            navigateToGoogleSignIn,
            navigateToGoogleSignOut
        )
    }
}
