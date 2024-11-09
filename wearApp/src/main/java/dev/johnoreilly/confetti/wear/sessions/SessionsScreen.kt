package dev.johnoreilly.confetti.wear.sessions


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.scrollTransform
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import kotlinx.datetime.toKotlinLocalDateTime

@Composable
fun SessionsScreen(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    addBookmark: ((sessionId: String) -> Unit)?,
    removeBookmark: ((sessionId: String) -> Unit)?,
) {
    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = rememberResponsiveColumnPadding(
                first = ColumnItemType.ListHeader,
                last = ColumnItemType.Card
            ),
        ) {
            when (uiState) {
                is SessionsUiState.Success -> {
                    val sessions = uiState.sessionsByStartTimeList.firstOrNull().orEmpty()

                    sessions.forEach { (time, sessionsAtTime) ->
                        item {
                            SectionHeader(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scrollTransform(this@item), text = time
                            )
                        }

                        items(sessionsAtTime) { session ->
                            SessionCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scrollTransform(this@items),
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
    ConfettiThemeFixed {
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
            ),
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {}
        )
    }

}