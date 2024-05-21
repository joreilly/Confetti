@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.home.HomeScreen
import dev.johnoreilly.confetti.wear.home.HomeUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.preview.TestFixtures.kotlinConf2023
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.WearDevice
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.time.LocalDateTime

@RunWith(ParameterizedRobolectricTestRunner::class)
class ConferenceHomeScreenTest(override val device: WearDevice) : BaseScreenshotTest() {

    init {
        tolerance = 0.03f
    }

    @Test
    fun conferenceHomeScreen() {
        composeRule.setContent {
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
        }
        composeRule.onNodeWithText("KotlinConf 2023").assertIsDisplayed()
        takeScreenshot()

        // Disabled temporarily, hangs roborazzi
//        composeRule.onNode(hasScrollToIndexAction())
//            .scrollToBottom()
//        takeScreenshot("_end")
    }

    @Test
    fun conferenceHomeScreenWithBookmarks() {
        composeRule.setContent {
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
        }
        takeScreenshot()

        // Disabled temporarily, hangs roborazzi
//        composeRule.onNode(hasScrollToIndexAction())
//            .scrollToBottom()
//        takeScreenshot("_end")
    }

    @Test
    fun conferenceHomeScreenA11y() {
        enableA11yTest()

        composeRule.setContent {
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
        }
        takeScreenshot()

        // Disabled temporarily, hangs roborazzi
//        composeRule.onNode(hasScrollToIndexAction())
//            .scrollToBottom()
//        takeScreenshot("_end")

        composeRule.onNodeWithText("KotlinConf 2023")
            .assertIsDisplayed()
            .assertHasNoClickAction()

        composeRule.onNodeWithText("Wednesday")
            .assertIsDisplayed()
            // TODO https://github.com/google/horologist/issues/2039
//                    .assertHasClickAction()
            .assertTouchHeightIsEqualTo(52.dp)

    }

    @Test
    fun conferenceHomeScreenLoading() {
        // Placeholders are not stable
        tolerance = 1.0f

        composeRule.setContent {
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
        }
        takeScreenshot()

        // Disabled temporarily, hangs roborazzi
//        composeRule.onNode(hasScrollToIndexAction())
//            .scrollToBottom()
//        takeScreenshot("_end")
    }

    @Test
    fun conferenceHomeScreenLoadingA11y() {
        // Placeholders are not stable
        tolerance = 1.0f

        enableA11yTest()

        composeRule.setContent {
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
        }
        takeScreenshot()

        // Disabled temporarily, hangs roborazzi
//        composeRule.onNode(hasScrollToIndexAction())
//            .scrollToBottom()
//        takeScreenshot("_end")

        composeRule.onNodeWithText("Conference Days")
            .assertIsDisplayed()
            .assertHasNoClickAction()

        composeRule.onAllNodesWithContentDescription("")
            .assertCountEquals(2)
            .assertAll(hasClickAction())
            .assertAll(isNotEnabled())
    }
}