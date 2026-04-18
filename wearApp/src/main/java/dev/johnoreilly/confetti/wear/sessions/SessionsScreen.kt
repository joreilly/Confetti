package dev.johnoreilly.confetti.wear.sessions


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.ConfettiPreviewScaffold
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import kotlinx.datetime.toKotlinLocalDateTime

@Composable
fun SessionsScreen(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    addBookmark: ((sessionId: String) -> Unit)?,
    removeBookmark: ((sessionId: String) -> Unit)?,
    columnState: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    modifier: Modifier = Modifier,
) {
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
            when (uiState) {
                is SessionsUiState.Success -> {
                    val sessions = uiState.sessionsByStartTimeList.firstOrNull().orEmpty()

                    sessions.forEach { (time, sessionsAtTime) ->
                        item {
                            SectionHeader(
                                modifier = Modifier
                                    .fillMaxWidth(), text = time
                            )
                        }

                        items(sessionsAtTime) { session ->
                            SessionCard(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                session = session,
                                sessionSelected = {
                                    sessionSelected(it)
                                },
                                currentTime = uiState.now,
                                isBookmarked = uiState.bookmarks.contains(session.id),
                                addBookmark = addBookmark,
                                removeBookmark = removeBookmark,
                            )
                        }
                    }
                }

                else -> {
                    // TODO
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun SessionListViewPreview() {
    ConfettiPreviewScaffold {
        SessionsScreen(
            uiState = SessionsUiState.Success(
                now = java.time.LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime(),
                conferenceName = "wearconf",
                conference = "wearconf",
                venueLat = null,
                venueLon = null,
                confDates = listOf(),
                formattedConfDates = listOf(),
                sessionsByStartTimeList = listOf(
                    mapOf(
                        "14:00" to listOf(
                            TestFixtures.sessionDetails
                        )
                    )
                ),
                speakers = listOf(),
                rooms = listOf(),
                bookmarks = setOf(),
                isRefreshing = false,
                searchString = "",
                selectedSessionId = null,
                notificationsActive = false,
            ),
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {}
        )
    }
}

@WearPreviewLargeRound
@ScrollingPreview(mode = ScrollMode.LONG)
@Composable
fun SessionListViewLongPreview() {
    ConfettiPreviewScaffold {
        SessionsScreen(
            uiState = SessionsUiState.Success(
                now = java.time.LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime(),
                conferenceName = "wearconf",
                conference = "wearconf",
                venueLat = null,
                venueLon = null,
                confDates = listOf(),
                formattedConfDates = listOf(),
                sessionsByStartTimeList = listOf(
                    mapOf(
                        "14:00" to listOf(
                            TestFixtures.sessionDetails,
                            TestFixtures.sessionDetails.copy(id = "a2", title = "Kotlin DSLs in Practice"),
                        ),
                        "15:00" to listOf(
                            TestFixtures.sessionDetails.copy(id = "b1", title = "Coroutines + Flow Recipes"),
                            TestFixtures.sessionDetails.copy(id = "b2", title = "Wear Tiles Deep Dive"),
                        ),
                        "16:00" to listOf(
                            TestFixtures.sessionDetails.copy(id = "c1", title = "Material 3 Expressive"),
                        ),
                    )
                ),
                speakers = listOf(),
                rooms = listOf(),
                bookmarks = setOf(),
                isRefreshing = false,
                searchString = "",
                selectedSessionId = null,
                notificationsActive = false,
            ),
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {}
        )
    }
}