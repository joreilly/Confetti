@file:OptIn(ExperimentalCoilApi::class)

package dev.johnoreilly.confetti.wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import coil.annotation.ExperimentalCoilApi
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.home.HomeScreen
import dev.johnoreilly.confetti.wear.home.HomeUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.preview.TestFixtures.kotlinConf2023
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime

@WearPreviewLargeRound
@WearPreviewSmallRound
@WearPreviewFontScales
@Composable
fun ConferenceHomeScreen() {
    TestScaffold {
        HomeScreen(
            uiState = QueryResult.Success(
                HomeUiState(
                    kotlinConf2023.id,
                    kotlinConf2023.name,
                    kotlinConf2023.days,
                )
            ),
            bookmarksUiState = QueryResult.None,
            sessionSelected = {},
            daySelected = {},
            onSettingsClick = {},
            onBookmarksClick = {},
        )
    }

//    composeRule.onNode(hasScrollToIndexAction())
//        .scrollToBottom()
//    takeScreenshot("_end")
}

@WearPreviewLargeRound
@WearPreviewSmallRound
@WearPreviewFontScales
@Composable
fun ConferenceHomeScreenWithBookmarks() {
    TestScaffold {
        HomeScreen(
            uiState = QueryResult.Success(
                HomeUiState(
                    kotlinConf2023.id,
                    kotlinConf2023.name,
                    kotlinConf2023.days,
                )
            ),
            bookmarksUiState = QueryResult.Success(
                BookmarksUiState(
                    kotlinConf2023.id,
                    listOf(
                        TestFixtures.sessionDetails,
                        TestFixtures.sessionDetails.copy(title = "Adopting Kotlin at Google scale")
                    ),
                    listOf(),
                    LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime()
                )
            ),
            sessionSelected = {},
            daySelected = {},
            onSettingsClick = {},
            onBookmarksClick = {},
        )
    }

//    composeRule.onNode(hasScrollToIndexAction())
//        .scrollToBottom()
//    takeScreenshot("_end")
}

@WearPreviewLargeRound
@WearPreviewSmallRound
@WearPreviewFontScales
@Composable
fun ConferenceHomeScreenLoading() {
    TestScaffold {
        HomeScreen(
            uiState = QueryResult.Loading,
            bookmarksUiState = QueryResult.Loading,
            sessionSelected = {},
            daySelected = {},
            onSettingsClick = {},
            onBookmarksClick = {},
        )
    }

//    composeRule.onNode(hasScrollToIndexAction())
//        .scrollToBottom()
//    takeScreenshot("_end")
}
