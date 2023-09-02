@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.conferences

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.ChipDefaults
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.PlaceholderChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.Chip
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes

@Composable
fun ConferencesRoute(
    component: ConferencesComponent,
    columnState: ScalingLazyColumnState
) {
    val uiState by component.uiState.subscribeAsState()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState !is ConferencesComponent.Loading
        }
    }

    ConferencesView(
        uiState = uiState,
        columnState = columnState,
        navigateToConference = { conference ->
            component.onConferenceClicked(conference)
        }
    )
}

@Composable
fun ConferencesView(
    uiState: ConferencesComponent.UiState,
    navigateToConference: (GetConferencesQuery.Conference) -> Unit,
    columnState: ScalingLazyColumnState,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(), columnState = columnState
    ) {
        item {
            SectionHeader("Conferences")
        }

        when (uiState) {
            is ConferencesComponent.Loading -> {
                items(5) {
                    PlaceholderChip(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ChipDefaults.secondaryChipColors()
                    )
                }
            }

            is ConferencesComponent.Success -> {
                // TODO show current year
                items(uiState.relevantConferences) { conference ->
                    Chip(
                        modifier = Modifier.fillMaxWidth(),
                        label = conference.name,
                        onClick = {
                            navigateToConference(conference)
                        },
                        colors = ChipDefaults.secondaryChipColors()
                    )
                }
            }

            else -> {
                // TODO
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun ConferencesViewPreview() {
    ConfettiTheme {
        ConferencesView(
            uiState = ConferencesComponent.Success(
                TestFixtures.conferences.groupBy { it.days[0].year }
            ),
            navigateToConference = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
