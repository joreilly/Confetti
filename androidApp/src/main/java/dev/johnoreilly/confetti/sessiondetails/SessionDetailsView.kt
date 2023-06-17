@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package dev.johnoreilly.confetti.sessiondetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.decompose.SessionDetailsComponent
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.Bookmark
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.SignInDialog
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.utils.format
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import java.time.format.DateTimeFormatter

@Composable
fun SessionDetailsRoute(
    component: SessionDetailsComponent,
) {
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
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { popBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            val horizontalPadding = 16.dp
            session?.let { session ->
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .verticalScroll(state = scrollState)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                            text = session.title,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.size(16.dp))

                        Text(
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                            text = session.startsAt.toTimeString(session.endsAt),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        session.room?.name?.let { roomName ->
                            Text(
                                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 2.dp),
                                text = roomName,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelLarge.copy(fontStyle = FontStyle.Italic)
                            )
                        }

                        Spacer(modifier = Modifier.size(16.dp))

                        Text(
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                            text = session.sessionDescription ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        DisableSelection {
                            if (session.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.size(16.dp))
                                FlowRow(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                                    session.tags.distinct().forEach { tag ->
                                        Box(Modifier.padding(bottom = 8.dp)) {
                                            Chip(tag)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.size(16.dp))
                            }
                        }

                        DisableSelection {
                            ConfettiHeader(
                                text = stringResource(R.string.speakers),
                                icon = Icons.Filled.Person
                            )
                        }

                        Spacer(modifier = Modifier.size(16.dp))

                        session.speakers.forEach { speaker ->
                            SessionSpeakerInfo(
                                speaker = speaker.speakerDetails,
                                onSocialLinkClick = { socialItem, _ ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialItem.url))
                                    context.startActivity(intent)
                                },
                                onSpeakerClick = onSpeakerClick,
                            )
                        }
                    }
                }
                if (showDialog) {
                    SignInDialog(
                        onDismissRequest = { showDialog = false },
                        onSignInClicked = navigateToSignIn
                    )
                }
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

private fun LocalDateTime.toTimeString(endsAt: LocalDateTime): String {
    val startTimeFormatter = DateTimeFormatter.ofPattern("MMM d HH:mm")
    val endTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTimeDate = startTimeFormatter.format(this)
    val endsAtTime = endTimeFormatter.format(endsAt)
    return "$startTimeDate - $endsAtTime"
}

@Composable
fun Chip(name: String) {
    Surface(
        modifier = Modifier.padding(end = 10.dp),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(10.dp)
        )
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
        return@remember { context.startActivity(launchIntent) }
    }
}
