@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.conferences

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.items
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.base.ui.components.StandardChip
import com.google.android.horologist.composables.PlaceholderChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import org.koin.androidx.compose.getViewModel

@Composable
fun ConferencesRoute(
    navigateToConference: (String) -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: ConferencesViewModel = getViewModel()
) {
    val uiState by viewModel.conferenceList.collectAsStateWithLifecycle()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState !is ConferencesUiState.Loading
        }
    }

    ConferencesView(
        uiState = uiState,
        columnState = columnState,
        navigateToConference = { conference ->
            viewModel.setConference(conference)
            navigateToConference(conference)
        }
    )
}

@Composable
fun ConferencesView(
    uiState: ConferencesUiState,
    navigateToConference: (String) -> Unit,
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
            is ConferencesUiState.Loading -> {
                items(5) {
                    PlaceholderChip(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            is ConferencesUiState.Success -> {
                items(uiState.conferences) { conference ->
                    StandardChip(
                        modifier = Modifier.fillMaxWidth(),
                        label = conference.name,
                        onClick = {
                            navigateToConference(conference.id)
                        },
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
            uiState = ConferencesUiState.Success(
                listOf(
                    GetConferencesQuery.Conference("0", "", emptyList(), "Droidcon San Francisco 2022"),
                    GetConferencesQuery.Conference("1", "", emptyList(), "FrenchKit 2022"),
                    GetConferencesQuery.Conference("2", "", emptyList(), "Droidcon London 2022"),
                    GetConferencesQuery.Conference("3", "", emptyList(), "DevFest Ukraine 2023"),
                )
            ),
            navigateToConference = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
