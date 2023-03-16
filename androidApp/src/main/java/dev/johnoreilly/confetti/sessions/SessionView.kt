package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.SessionsViewModel
import dev.johnoreilly.confetti.account.Authentication
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.sessionSpeakerLocation
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@Composable
fun SessionsRoute(
    isExpandedScreen: Boolean,
    @Suppress("UNUSED_PARAMETER") displayFeatures: List<DisplayFeature>,
    navigateToSession: (String) -> Unit,
    navigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    conference: String,
) {
    val viewModel: SessionsViewModel = getViewModel()

    // handled in AppViewModel now?
    //viewModel.setConference(conference)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (isExpandedScreen) {
        SessionListGridView(
            uiState,
            navigateToSession,
            navigateToSignIn,
            onSignOut,
            onSwitchConferenceSelected
        )
    } else {
        SessionListView(
            uiState,
            navigateToSession,
            { viewModel.addBookmark(it) },
            { viewModel.removeBookmark(it) },
            navigateToSignIn,
            onSignOut,
            onSwitchConferenceSelected,
            viewModel::refresh2
        )
    }
}


@Composable
fun SessionView(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit,
    isBookmarked: Boolean,
    addBookmark: (String) -> Unit,
    removeBookmark: (String) -> Unit
) {

    var modifier = Modifier.fillMaxSize()
    if (!session.isBreak()) {
        modifier = modifier.clickable(onClick = {
            sessionSelected(session.id)
        })
    }
    Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = session.title, style = TextStyle(fontSize = 16.sp))
            }

            session.room?.let {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        session.sessionSpeakerLocation(),
                        style = TextStyle(fontSize = 14.sp), fontWeight = FontWeight.Bold
                    )
                }
            }
        }


        val authentication = get<Authentication>()
        val user by remember { mutableStateOf(authentication.currentUser()) }
        if (user != null) {
            if (isBookmarked) {
                Icon(
                    imageVector = Icons.Outlined.Bookmark,
                    contentDescription = "remove bookmark",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { removeBookmark(session.id) }.padding(8.dp))
            } else {
                Icon(
                    imageVector = Icons.Outlined.BookmarkAdd,
                    contentDescription = "add bookmark",
                    modifier = Modifier.clickable { addBookmark(session.id) }.padding(8.dp))
            }
        }
    }
}
