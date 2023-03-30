@file:OptIn(ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.bookmarks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.sessions.SessionItemView
import dev.johnoreilly.confetti.ui.ConfettiAppState
import dev.johnoreilly.confetti.ui.ConfettiScaffold
import dev.johnoreilly.confetti.ui.LoadingView

@Composable
fun BookmarksView(
    conference: String,
    appState: ConfettiAppState,
    sessions: List<SessionDetails>,
    navigateToSession: (SessionDetailsKey) -> Unit,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    loading: Boolean,
) {
    ConfettiScaffold(
        title = stringResource(R.string.bookmarks),
        conference = conference,
        appState = appState,
        onSwitchConference = onSwitchConference,
        onSignIn = onSignIn,
        onSignOut = onSignOut,
    ) {
        val user = it.user
        Column {

            if (loading) {
                LoadingView()
            } else {
                LazyColumn {
                    items(sessions) { session ->
                        SessionItemView(
                            conference = conference,
                            session = session,
                            sessionSelected = navigateToSession,
                            isBookmarked = bookmarks.contains(session.id),
                            addBookmark = { sessionId -> addBookmark(sessionId) },
                            removeBookmark = { sessionId -> removeBookmark(sessionId) },
                            onNavigateToSignIn = onSignIn,
                            user = user,
                        )
                    }
                }
            }
        }
    }
}
