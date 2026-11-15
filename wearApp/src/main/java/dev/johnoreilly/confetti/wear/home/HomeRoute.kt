package dev.johnoreilly.confetti.wear.home

import dev.johnoreilly.confetti.wear.components.WearComponents
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.utils.QueryResult

context(components: WearComponents)
@Composable
fun HomeRoute(
    component: HomeComponent,
) {
    val uiState = component.uiState.collectAsStateWithLifecycle().value
    val bookmarksUiState by component.bookmarksUiState.collectAsStateWithLifecycle()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState !is QueryResult.Loading && bookmarksUiState !is QueryResult.Loading
        }
    }

        HomeScreen(
            uiState = uiState,
            bookmarksUiState = bookmarksUiState,
            sessionSelected = {
                component.onSessionClicked(it)
            },
            daySelected = {
                component.onDayClicked(it)
            },
            onSettingsClick = {
                component.onSettingsClicked()
            },
            onBookmarksClick = {
                component.onBookmarksClick()
            },
            addBookmark = { component.addBookmark(it) },
            removeBookmark = { component.removeBookmark(it) },
        )
}
