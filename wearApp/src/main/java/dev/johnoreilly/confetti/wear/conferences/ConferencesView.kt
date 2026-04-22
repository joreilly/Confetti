package dev.johnoreilly.confetti.wear.conferences

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.wear.components.PlaceholderButton
import dev.johnoreilly.confetti.wear.components.ScreenHeader
import dev.johnoreilly.confetti.wear.preview.ConfettiPreviewScaffold
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.seedColor
import dev.johnoreilly.confetti.wear.ui.toColor

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
    modifier: Modifier = Modifier,
    columnState: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
) {
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(
        modifier = modifier,
        scrollState = columnState,
        scrollIndicator = {
            if (!LocalScrollCaptureInProgress.current) {
                ScrollIndicator(columnState)
            }
        },
    ) { contentPadding ->
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = contentPadding,
        ) {
            item {
                ScreenHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                        .minimumVerticalContentPadding(
                            ListHeaderDefaults.minimumTopListContentPadding
                        ),
                    text = "Conferences",
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }

            when (uiState) {
                is ConferencesComponent.Loading -> {
                    items(5) {
                        PlaceholderButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .minimumVerticalContentPadding(
                                    ButtonDefaults.minimumVerticalListContentPadding
                                ),
                            transformation = SurfaceTransformation(transformationSpec),
                        )
                    }
                }

                is ConferencesComponent.Success -> {
                    // TODO show current year
                    items(uiState.relevantConferences) { conference ->
                        ConferencesChip(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .minimumVerticalContentPadding(
                                    ButtonDefaults.minimumVerticalListContentPadding
                                ),
                            conference = conference,
                            navigateToConference = navigateToConference,
                            transformation = SurfaceTransformation(transformationSpec),
                        )
                    }
                }

                else -> {
                    // TODO
                }
            }
        }
    }
}

@Composable
private fun ConferencesChip(
    modifier: Modifier = Modifier,
    conference: GetConferencesQuery.Conference,
    navigateToConference: (GetConferencesQuery.Conference) -> Unit,
    transformation: SurfaceTransformation? = null,
) {
    // Curated conference themes (KotlinConf, AndroidMakers, Droidcon,
    // DevFest) win over the backend-supplied themeColor so each chip on
    // the conference list carries its own brand seed — not a generic
    // fallback the backend happens to have stored. See
    // `Conference.seedColor()` for the resolution order.
    ConfettiTheme(seedColor = conference.seedColor()) {
        Button(
            modifier = modifier.fillMaxWidth(),
            transformation = transformation,
            onClick = {
                navigateToConference(conference)
            },
            colors = ButtonDefaults.filledVariantButtonColors()
        ) { Text(conference.name) }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun ConferencesViewPreview() {
    ConfettiPreviewScaffold {
        ConferencesView(
            uiState = ConferencesComponent.Success(
                TestFixtures.conferences.groupBy { it.days[0].year }
            ),
            navigateToConference = {},
        )
    }
}

@WearPreviewLargeRound
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun ConferencesViewLongPreview() {
    ConfettiPreviewScaffold {
        ConferencesView(
            uiState = ConferencesComponent.Success(
                TestFixtures.conferences.groupBy { it.days[0].year }
            ),
            navigateToConference = {},
        )
    }
}

/**
 * List view of the four curated-theme conferences. Each chip wraps itself
 * in a [ConfettiTheme] seeded by `conferenceThemeFor(id)` so the colors
 * should land as KotlinConf purple, AndroidMakers ochre, Droidcon green,
 * and DevFest blue — not a repeated backend-default red.
 */
@WearPreviewLargeRound
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun ConferencesViewCuratedPreview() {
    val curated = listOf(
        dev.johnoreilly.confetti.wear.preview.ConferenceFixtures.kotlinConf,
        dev.johnoreilly.confetti.wear.preview.ConferenceFixtures.androidMakers,
        dev.johnoreilly.confetti.wear.preview.ConferenceFixtures.droidcon,
        dev.johnoreilly.confetti.wear.preview.ConferenceFixtures.devFest,
    )
    ConfettiPreviewScaffold {
        ConferencesView(
            uiState = ConferencesComponent.Success(curated.groupBy { it.days[0].year }),
            navigateToConference = {},
        )
    }
}

