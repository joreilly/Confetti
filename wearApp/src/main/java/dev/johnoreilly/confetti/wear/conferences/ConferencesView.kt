@file:OptIn(ExperimentalWearMaterialApi::class)

package dev.johnoreilly.confetti.wear.conferences

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.placeholderShimmer
import androidx.wear.compose.material.rememberPlaceholderState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.wear.components.ScreenHeader
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.toColor
import androidx.wear.compose.material.Chip as MaterialChip

@Composable
fun ConferencesRoute(
    component: ConferencesComponent,
) {
    val uiState by component.uiState.subscribeAsState()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState !is ConferencesComponent.Loading
        }
    }

    ConferencesView(
        uiState = uiState,
        navigateToConference = { conference ->
            component.onConferenceClicked(conference)
        }
    )
}

@Composable
fun ConferencesView(
    uiState: ConferencesComponent.UiState,
    navigateToConference: (GetConferencesQuery.Conference) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnState: ScalingLazyColumnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            modifier = modifier.fillMaxSize(), columnState = columnState
        ) {
            item {
                ScreenHeader("Conferences")
            }

            val conferences = when (uiState) {
                is ConferencesComponent.Loading -> {
                    List(5) { null }
                }

                is ConferencesComponent.Success -> {
                    uiState.relevantConferences
                }

                else -> {
                    listOf()
                }
            }

            // TODO show current year
            items(conferences) { conference ->
                ConferencesChip(conference, navigateToConference)
            }
        }
    }
}

@Composable
private fun ConferencesChip(
    conference: GetConferencesQuery.Conference?,
    navigateToConference: (GetConferencesQuery.Conference) -> Unit
) {
    val chipPlaceholderState = rememberPlaceholderState {
        conference != null
    }

    val seedColor = conference?.themeColor?.toColor()

    LaunchedEffect(conference?.id) {
        println("Conference ${conference?.id}")
    }

    LaunchedEffect(chipPlaceholderState) {
        println("Placeholder content:${chipPlaceholderState.isShowContent} wipeoff:${chipPlaceholderState.isWipeOff}")
    }

    ConfettiTheme(seedColor = seedColor) {
        MaterialChip(
            label = {
                Text(
                    text = conference?.name.orEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(chipPlaceholderState),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )
            },
            onClick = {
                if (conference != null) {
                    navigateToConference(conference)
                }
            },
            modifier = Modifier
                .placeholderShimmer(chipPlaceholderState)
                .fillMaxWidth(),
            colors = ChipDefaults.secondaryChipColors(),
            enabled = conference != null,
        )
    }

    if (!chipPlaceholderState.isShowContent) {
        println("Launching placeholder animation")
        LaunchedEffect(chipPlaceholderState) { chipPlaceholderState.startPlaceholderAnimation() }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun ConferencesViewPreview() {
    ConferencesView(
        uiState = ConferencesComponent.Success(
            TestFixtures.conferences.groupBy { it.days[0].year }
        ),
        navigateToConference = {},
    )
}
