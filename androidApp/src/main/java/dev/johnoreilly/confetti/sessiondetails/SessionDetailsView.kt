@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.sessiondetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.decompose.SessionDetailsComponent
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.Bookmark
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.SessionDetailViewSharedWrapper
import dev.johnoreilly.confetti.ui.SignInDialog
import dev.johnoreilly.confetti.utils.format
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.compose.koinInject
import java.time.format.DateTimeFormatter

@Composable
fun SessionDetailsRoute(component: SessionDetailsComponent) {

    val user by koinInject<Authentication>().currentUser.collectAsStateWithLifecycle()
    val uiState by component.uiState.subscribeAsState()
    val isBookmarked by component.isBookmarked.collectAsStateWithLifecycle()

    val addErrorCount by component.addErrorChannel.receiveAsFlow()
        .collectAsStateWithLifecycle(initialValue = 0)
    val removeErrorCount by component.removeErrorChannel.receiveAsFlow()
        .collectAsStateWithLifecycle(initialValue = 0)

    when (val state = uiState) {
        is SessionDetailsUiState.Loading -> LoadingView()
        is SessionDetailsUiState.Error -> ErrorView()

        is SessionDetailsUiState.Success ->
            SessionDetailView(
                session = state.sessionDetails,
                popBack = component::onCloseClicked,
                share = rememberShareDetails(state.sessionDetails),
                addBookmark = component::addBookmark,
                removeBookmark = component::removeBookmark,
                isUserLoggedIn = user != null,
                isBookmarked = isBookmarked,
                navigateToSignIn = component::onSignInClicked,
                addErrorCount = addErrorCount,
                removeErrorCount = removeErrorCount,
                onSpeakerClick = component::onSpeakerClicked,
            )
    }
}

@Composable
fun SessionDetailView(
    session: SessionDetails?,
    popBack: () -> Unit,
    share: () -> Unit,
    addBookmark: () -> Unit,
    removeBookmark: () -> Unit,
    navigateToSignIn: () -> Unit,
    isUserLoggedIn: Boolean,
    isBookmarked: Boolean,
    addErrorCount: Int,
    removeErrorCount: Int,
    onSpeakerClick: (speakerId: String) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { popBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { share() }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    Bookmark(
                        isBookmarked = isBookmarked,
                        onBookmarkChange = { shouldAdd ->
                            if (!isUserLoggedIn) {
                                showDialog = true
                                return@Bookmark
                            }
                            if (shouldAdd) {
                                addBookmark()
                            } else {
                                removeBookmark()
                            }
                        }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) {
        Column(modifier = Modifier.padding(it)) {

            SessionDetailViewSharedWrapper(
                session = session,
                onSpeakerClick = { speakerId ->  onSpeakerClick(speakerId) },
                onSocialLinkClicked =  { socialItem ->
                    runCatching {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialItem))
                        context.startActivity(intent)
                    }.getOrElse { error ->
                        error.printStackTrace()
                    }
            })

            if (showDialog) {
                SignInDialog(
                    onDismissRequest = { showDialog = false },
                    onSignInClicked = navigateToSignIn
                )
            }
        }

        LaunchedEffect(addErrorCount) {
            if (addErrorCount > 0) {
                snackbarHostState.showSnackbar(
                    message = "Error while adding bookmark",
                    duration = SnackbarDuration.Short,
                )
            }
        }

        LaunchedEffect(removeErrorCount) {
            if (removeErrorCount > 0) {
                snackbarHostState.showSnackbar(
                    message = "Error while removing bookmark",
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
}


@Composable
private fun rememberShareDetails(details: SessionDetails?): () -> Unit {
    val context = LocalContext.current

    return remember(context, details) {
        // If details is null, there is nothing to share.
        if (details == null) return@remember {}

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"

            val room = details.room?.name ?: "Unknown"
            val date = dateFormatter.format(details.startsAt)
            val startsAt = timeFormatter.format(details.startsAt)
            val endsAt = timeFormatter.format(details.endsAt)
            val schedule = "$date $startsAt-$endsAt"
            val speakers = details
                .speakers
                .map { it.speakerDetails.name }
                .toString()
                .removeSurrounding(prefix = "[", suffix = "]")

            val text =
                """
                |Title: ${details.title}
                |Schedule: $schedule
                |Room: $room
                |Speaker: $speakers
                |---
                |Description: ${details.sessionDescription}
                """.trimMargin()
            putExtra(Intent.EXTRA_TEXT, text)
        }

        val launchIntent = Intent.createChooser(sendIntent, null)
        return@remember {
            runCatching {
                context.startActivity(launchIntent)
            }.getOrElse { error ->
                error.printStackTrace()
            }
        }
    }
}
