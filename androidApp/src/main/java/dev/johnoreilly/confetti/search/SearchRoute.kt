package dev.johnoreilly.confetti.search

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.decompose.SearchComponent
import dev.johnoreilly.confetti.ui.HomeScaffold

@Composable
fun SearchRoute(
    component: SearchComponent,
    windowSizeClass: WindowSizeClass,
    topBarActions: @Composable RowScope.() -> Unit,
) {
    val search by component
        .search
        .collectAsStateWithLifecycle(initialValue = "")
    val loading by component
        .loading
        .collectAsStateWithLifecycle(initialValue = true)
    val sessions by component
        .sessions
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val bookmarks by component
        .bookmarks
        .collectAsStateWithLifecycle(initialValue = emptySet())
    val speakers by component
        .speakers
        .collectAsStateWithLifecycle(initialValue = emptyList())

    HomeScaffold(
        title = stringResource(id = R.string.search),
        windowSizeClass = windowSizeClass,
        topBarActions = topBarActions,
    ) {
        SearchView(
            navigateToSession = component::onSessionClicked,
            navigateToSpeaker = component::onSpeakerClicked,
            onSignIn = component::onSignInClicked,
            sessions = sessions,
            speakers = speakers,
            search = search,
            onSearchChange = component::onSearchChange,
            bookmarks = bookmarks,
            addBookmark = component::addBookmark,
            removeBookmark = component::removeBookmark,
            loading = loading,
            isLoggedIn = component.isLoggedIn,
        )
    }
}
