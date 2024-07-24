package dev.johnoreilly.confetti.bookmarks

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.bookmarks
import dev.johnoreilly.confetti.decompose.BookmarksComponent
import dev.johnoreilly.confetti.ui.HomeScaffold
import org.jetbrains.compose.resources.stringResource

@Composable
fun BookmarksRoute(
    component: BookmarksComponent,
    windowSizeClass: WindowSizeClass,
    topBarNavigationIcon: @Composable () -> Unit = {},
    topBarActions: @Composable RowScope.() -> Unit = {},
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
        title = stringResource(Res.string.bookmarks),
        windowSizeClass = windowSizeClass,
        topBarNavigationIcon = topBarNavigationIcon,
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
