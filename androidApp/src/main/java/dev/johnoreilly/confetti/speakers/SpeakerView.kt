package dev.johnoreilly.confetti.speakers

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.ui.ConfettiAppState
import dev.johnoreilly.confetti.ui.ConfettiScaffold
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpeakersRoute(
    conference: String,
    appState: ConfettiAppState,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    val viewModel: SpeakersAndroidViewModel = koinViewModel<SpeakersAndroidViewModel>().apply {
        configure(conference = conference)
    }
    val speakers: LazyPagingItems<SpeakerDetails> = viewModel.speakers.collectAsLazyPagingItems()
    val state = speakers.loadState

    ConfettiScaffold(
        title = stringResource(R.string.speakers),
        conference = conference,
        appState = appState,
        onSwitchConference = onSwitchConference,
        onSignIn = onSignIn,
        onSignOut = onSignOut,
    ) {
        when (state.refresh) {
            is LoadState.NotLoading -> {
                if (appState.isExpandedScreen) {
                    SpeakerGridView(conference, speakers, navigateToSpeaker)
                } else {
                    SpeakerListView(conference, speakers, navigateToSpeaker)
                }
            }

            is LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView {

            }
        }
    }

}


@Composable
fun SpeakerGridView(
    conference: String,
    speakers: LazyPagingItems<SpeakerDetails>,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier.padding(16.dp),
        columns = GridCells.Adaptive(200.dp),

        // content padding
        contentPadding = PaddingValues(8.dp),
        content = {
            items(speakers) { speaker ->
                if (speaker == null) return@items
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (speaker.photoUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = speaker.photoUrl,
                            contentDescription = speaker.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Spacer(modifier = Modifier.size(150.dp))
                    }

                    Text(
                        text = speaker.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )

                }
            }
            item {
                if (speakers.loadState.append == LoadState.Loading) {
                    LoadingItem()
                }
            }
        }
    )
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SpeakerListView(
    conference: String,
    speakers: LazyPagingItems<SpeakerDetails>,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit
) {
    Column {
        LazyColumn {
            items(speakers) { speaker ->
                if (speaker != null) SpeakerItemView(conference, speaker, navigateToSpeaker)
            }
            item {
                if (speakers.loadState.append == LoadState.Loading) {
                    LoadingItem()
                }
            }
        }
    }
}


@Composable
fun SpeakerItemView(
    conference: String,
    speaker: SpeakerDetails,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { navigateToSpeaker(SpeakerDetailsKey(conference, speaker.id)) })
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (speaker.photoUrl?.isNotEmpty() == true) {
            AsyncImage(
                model = speaker.photoUrl,
                contentDescription = speaker.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.size(60.dp))
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(text = speaker.name, style = TextStyle(fontSize = 20.sp))
            Text(
                text = speaker.company ?: "",
                style = TextStyle(color = Color.DarkGray, fontSize = 14.sp)
            )
        }
    }
}

@Composable
private fun LoadingItem() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CircularProgressIndicator()
    }
}

// Copied from the Paging library, but with LazyGridScope instead of LazyListScope
private fun <T : Any> LazyGridScope.items(
    items: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyGridItemScope.(value: T?) -> Unit
) {
    items(
        count = items.itemCount,
        key = if (key == null) null else { index ->
            val item = items.peek(index)
            if (item == null) {
                PagingPlaceholderKey(index)
            } else {
                key(item)
            }
        }
    ) { index ->
        itemContent(items[index])
    }
}

// Copied from the Paging library (as it is private)
@SuppressLint("BanParcelableUsage")
private data class PagingPlaceholderKey(private val index: Int) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingPlaceholderKey> =
            object : Parcelable.Creator<PagingPlaceholderKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingPlaceholderKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingPlaceholderKey?>(size)
            }
    }
}
