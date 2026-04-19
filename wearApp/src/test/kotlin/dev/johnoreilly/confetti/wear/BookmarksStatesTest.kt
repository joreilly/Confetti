@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksScreen
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.WearDevice
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDateTime

/**
 * Low-signal data-variant coverage for [BookmarksScreen]: loading, error,
 * and empty. Pinned to a single device — these states are primarily about
 * content presence, not layout scaling, so running them across the device
 * triad used by [BookmarksTest] would only duplicate pixels.
 */
@RunWith(RobolectricTestRunner::class)
class BookmarksStatesTest : BaseScreenshotTest() {
    init {
        tolerance = 0.02f
    }

    override val device: WearDevice = WearDevice.GenericLargeRound

    @Test
    fun bookmarksLoading() {
        composeRule.setContent {
            TestScaffold {
                BookmarksScreen(
                    uiState = QueryResult.Loading,
                    sessionSelected = {},
                    addBookmark = {},
                    removeBookmark = {})
            }
        }
        takeScreenshot()
    }

    @Test
    fun bookmarksError() {
        composeRule.setContent {
            TestScaffold {
                BookmarksScreen(
                    uiState = QueryResult.Error(Exception("Some Error")),
                    sessionSelected = {},
                    addBookmark = {},
                    removeBookmark = {})
            }
        }
        takeScreenshot()
    }

    @Test
    fun bookmarksEmpty() {
        composeRule.setContent {
            TestScaffold {
                BookmarksScreen(
                    uiState = QueryResult.Success(
                        BookmarksUiState(
                            conference = "wearconf",
                            upcoming = emptyList(),
                            past = emptyList(),
                            now = LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime()
                        )
                    ), sessionSelected = {}, addBookmark = {}, removeBookmark = {})
            }
        }
        takeScreenshot()
    }
}
