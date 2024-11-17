@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksScreen
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.WearDevice
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.time.LocalDateTime

@RunWith(ParameterizedRobolectricTestRunner::class)
class BookmarksTest(override val device: WearDevice) : BaseScreenshotTest() {
    init {
        tolerance = 0.02f
    }

    @Test
    fun bookmarks() {
        composeRule.setContent {
            TestScaffold {
                BookmarksScreen(
                    uiState = QueryResult.Success(
                        BookmarksUiState(
                            conference = TestFixtures.kotlinConf2023.id,
                            upcoming = listOf(TestFixtures.sessionDetails),
                            past = listOf(),
                            now = LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime()
                        )
                    ),
                    sessionSelected = {},
                    addBookmark = {},
                    removeBookmark = {}
                )
            }
        }
        takeScreenshot()
        // TODO enable in follow up PR
//        scrollToBottom()
//        takeScreenshot("_end")
    }
}