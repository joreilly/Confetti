@file:OptIn(ExperimentalLifecycleComposeApi::class)

package dev.johnoreilly.confetti.wear.sessiondetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.fragment.SessionDetails
import org.koin.androidx.compose.getViewModel

@Composable
fun SessionDetailsRoute(
    columnState: ScalingLazyColumnState,
    onBackClick: () -> Unit,
    viewModel: SessionDetailsViewModel = getViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    SessionDetailView(session, columnState, onBackClick)
}

@Composable
fun SessionDetailView(
    session: SessionDetails?,
    columnState: ScalingLazyColumnState,
    popBack: () -> Unit
) {
    val context = LocalContext.current

    ScalingLazyColumn(columnState = columnState) {
        session?.let { session ->
            item {
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.button,
                        color = MaterialTheme.colors.onSurfaceVariant
                    )
                }
            }

            if (session.description != null) {
                item {
                    Text(
                        text = session.description!!,
                        style = MaterialTheme.typography.body2
                    )
                }
            }

//            if (session.tags.isNotEmpty()) {
//                Spacer(modifier = Modifier.size(16.dp))
//                    FlowRow(crossAxisSpacing = 8.dp) {
//                        session.tags.forEach { tag ->
//                            Chip(tag)
//                        }
//                    }
//            }

            items(session.speakers) { speaker ->
                SessionSpeakerInfo(speaker = speaker.speakerDetails,
                    onSocialLinkClick = { socialItem, speakerDetails ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialItem.link))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

