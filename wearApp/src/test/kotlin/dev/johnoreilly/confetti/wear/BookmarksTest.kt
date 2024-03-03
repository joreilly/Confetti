@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.hasScrollToIndexAction
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksScreen
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.TestScaffold
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
                    sessionSelected = {}
                )
            }
        }
        takeScreenshot()
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
    }
}