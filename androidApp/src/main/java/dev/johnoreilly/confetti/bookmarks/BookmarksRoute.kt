package dev.johnoreilly.confetti.bookmarks

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.decompose.BookmarksComponent
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.ui.HomeScaffold

@Composable
fun BookmarksRoute(
    component: BookmarksComponent,
    windowSizeClass: WindowSizeClass,
    topBarActions: @Composable RowScope.() -> Unit,
) {
    val loading by component
        .loading
        .collectAsStateWithLifecycle(initialValue = true)
    val pastSessions by component
        .pastSessions
        .collectAsStateWithLifecycle(initialValue = emptyMap())
    val upcomingSessions by component
        .upcomingSessions
        .collectAsStateWithLifecycle(initialValue = emptyMap())
    val bookmarks by component
        .bookmarks
        .collectAsStateWithLifecycle(initialValue = emptySet())

    HomeScaffold(
        title = stringResource(R.string.bookmarks),
        windowSizeClass = windowSizeClass,
        topBarActions = topBarActions,
    ) {
        BookmarksView(
            navigateToSession = component::onSessionClicked,
            onSignIn = component::onSignInClicked,
            pastSessions = pastSessions,
            upcomingSessions = upcomingSessions,
            bookmarks = bookmarks,
            addBookmark = component::addBookmark,
            removeBookmark = component::removeBookmark,
            loading = loading,
            isLoggedIn = component.isLoggedIn,
        )
    }
}
