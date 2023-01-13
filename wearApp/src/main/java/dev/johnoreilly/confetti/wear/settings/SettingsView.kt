@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.compose.runtime.Composable
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.wear.ConfettiViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsRoute(
    columnState: ScalingLazyColumnState,
    onSwitchConferenceSelected: () -> Unit,
    viewModel: ConfettiViewModel = getViewModel(),
) {
    SettingsListView(
        conferenceCleared = {
            viewModel.clearConference()
            onSwitchConferenceSelected()
        },
        columnState = columnState
    )
}


