package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.avatarUrl
import dev.johnoreilly.confetti.isLightning
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.sessionsSuccessState
import dev.johnoreilly.confetti.ui.SignInDialog
import dev.johnoreilly.confetti.ui.component.ErrorView
import dev.johnoreilly.confetti.ui.component.LoadingView
import dev.johnoreilly.confetti.ui.icons.Bolt
import dev.johnoreilly.confetti.ui.icons.Bookmark
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

// Width of the leading time-axis column, and each room column
private val TimeAxisWidth = 64.dp
private val RoomColumnWidth = 280.dp

// Height of the room-name header row, shared by the pinned time axis (as a matching spacer)
// and the scrollable room columns, so the two stay aligned.
private val RoomHeaderHeight = 56.dp

// Vertical scale of the grid: how tall one minute of conference time renders as.
private val MinuteHeight = 3.dp

// Interval, in minutes, between horizontal gridlines/time labels.
private const val GridSlotMinutes = 30

// Below this rendered height, a session card switches to a compact, title-only layout.
private val CompactCardThreshold = 64.dp

@Composable
fun SessionListGridView(
    uiState: SessionsUiState,
    sessionSelected: (id: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    isLoggedIn: Boolean,
    onRefresh: () -> Unit,
) {
    when (uiState) {
        SessionsUiState.Error -> ErrorView(onRefresh)
        SessionsUiState.Loading -> LoadingView()

        is SessionsUiState.Success -> {
            Column {
                val pagerState = rememberPagerState {
                    uiState.formattedConfDates.size
                }

                SessionListTabRow(pagerState, uiState)

                HorizontalPager(state = pagerState) { page ->
                    SessionScheduleGrid(
                        conference = uiState.conference,
                        confDate = uiState.confDates[page],
                        sessionsByStartTime = uiState.sessionsByStartTimeList[page],
                        allRooms = uiState.rooms,
                        bookmarks = uiState.bookmarks,
                        sessionSelected = sessionSelected,
                        addBookmark = addBookmark,
                        removeBookmark = removeBookmark,
                        onNavigateToSignIn = onNavigateToSignIn,
                        isLoggedIn = isLoggedIn,
                    )
                }
            }
        }
    }
}

/**
 * Renders one conference day as a time-proportional grid: rooms as columns, a shared time axis
 * down the left, and sessions positioned/sized by their actual start/end time (like a calendar
 * day view) rather than bucketed into fixed-height rows. Sessions that genuinely overlap within
 * the same room are placed side-by-side as sub-columns instead of one silently hiding the other.
 */
@Composable
private fun SessionScheduleGrid(
    conference: String,
    confDate: LocalDate,
    sessionsByStartTime: Map<String, List<SessionDetails>>,
    allRooms: List<RoomDetails>,
    bookmarks: Set<String>,
    sessionSelected: (id: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    isLoggedIn: Boolean,
) {
    val daySessions = remember(sessionsByStartTime) {
        sessionsByStartTime.values.flatten().filter { it.room != null }
    }
    val rooms = remember(daySessions, allRooms) {
        allRooms.filter { room -> daySessions.any { it.room?.name == room.name } }
    }

    if (rooms.isEmpty() || daySessions.isEmpty()) {
        return
    }

    val gridStart = remember(daySessions) {
        floorToSlot(daySessions.minOf { it.startsAt.offsetMinutes(confDate) }, GridSlotMinutes)
    }
    val gridEnd = remember(daySessions) {
        ceilToSlot(daySessions.maxOf { it.endsAt.offsetMinutes(confDate) }, GridSlotMinutes)
    }

    val placedSessionsByRoom = remember(daySessions, rooms) {
        rooms.associateWith { room ->
            assignOverlapColumns(daySessions.filter { it.room?.name == room.name }, confDate)
        }
    }

    // The time intervals each room actually has a session running, so gridlines aren't drawn
    // across dead time in that room's column - whether that's before its first session, after
    // its last one, or a gap between two sessions (e.g. a room packed with short lightning talks
    // that don't quite line up with the other rooms' longer sessions).
    val roomSessionIntervals = remember(daySessions, rooms, confDate) {
        rooms.associateWith { room ->
            daySessions
                .filter { it.room?.name == room.name }
                .map { it.startsAt.offsetMinutes(confDate)..it.endsAt.offsetMinutes(confDate) }
        }
    }

    val verticalScrollState = rememberScrollState()
    val totalHeight = MinuteHeight * (gridEnd - gridStart)

    Row {
        // Pinned time axis: scrolls vertically in lockstep with the room columns (shared scroll
        // state) but stays put when the room columns scroll horizontally, so you don't lose track
        // of what time you're looking at once you've scrolled past the first couple of rooms.
        Column {
            Spacer(Modifier.height(RoomHeaderHeight))
            Box(Modifier.verticalScroll(verticalScrollState)) {
                Box(
                    Modifier
                        .width(TimeAxisWidth)
                        .height(totalHeight)
                        .padding(bottom = 16.dp)
                ) {
                    var slotStart = gridStart
                    while (slotStart <= gridEnd) {
                        val y = MinuteHeight * (slotStart - gridStart)
                        Text(
                            text = formatMinuteOfDay(slotStart),
                            modifier = Modifier
                                .offset(x = 0.dp, y = y - 8.dp)
                                .width(TimeAxisWidth - 8.dp),
                            textAlign = TextAlign.End,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        slotStart += GridSlotMinutes
                    }
                }
            }
        }

        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Row(Modifier.horizontalScroll(rememberScrollState())) {
            Column {
                Row(
                    modifier = Modifier.height(RoomHeaderHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    rooms.forEach { room ->
                        Text(
                            modifier = Modifier.width(RoomColumnWidth),
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            text = room.name
                        )
                    }
                }

                Box(Modifier.verticalScroll(verticalScrollState)) {
                    Box(
                        Modifier
                            .width(RoomColumnWidth * rooms.size)
                            .height(totalHeight)
                            .padding(bottom = 16.dp)
                    ) {
                        var slotStart = gridStart
                        while (slotStart <= gridEnd) {
                            val y = MinuteHeight * (slotStart - gridStart)
                            rooms.forEachIndexed { roomIndex, room ->
                                if (roomSessionIntervals.getValue(room).any { slotStart in it }) {
                                    Box(
                                        Modifier
                                            .offset(x = RoomColumnWidth * roomIndex, y = y)
                                            .width(RoomColumnWidth)
                                            .height(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant)
                                    )
                                }
                            }
                            slotStart += GridSlotMinutes
                        }

                        rooms.forEachIndexed { roomIndex, _ ->
                            Box(
                                Modifier
                                    .offset(x = RoomColumnWidth * roomIndex)
                                    .width(1.dp)
                                    .height(totalHeight)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }

                        rooms.forEachIndexed { roomIndex, room ->
                            placedSessionsByRoom.getValue(room).forEach { placed ->
                                val columnWidth = RoomColumnWidth / placed.columnCount
                                val x = RoomColumnWidth * roomIndex +
                                    columnWidth * placed.columnIndex
                                val y = MinuteHeight * (placed.startOffset - gridStart)
                                val height = MinuteHeight * (placed.endOffset - placed.startOffset)

                                SessionGridCard(
                                    conference = conference,
                                    session = placed.session,
                                    bookmarks = bookmarks,
                                    height = height,
                                    sessionSelected = sessionSelected,
                                    addBookmark = addBookmark,
                                    removeBookmark = removeBookmark,
                                    onNavigateToSignIn = onNavigateToSignIn,
                                    isLoggedIn = isLoggedIn,
                                    modifier = Modifier
                                        .offset(x = x, y = y)
                                        .width(columnWidth)
                                        .height(height)
                                        .padding(2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A session as placed within a single room column: its time offsets and overlap sub-column. */
private data class PlacedSession(
    val session: SessionDetails,
    val startOffset: Int,
    val endOffset: Int,
    val columnIndex: Int,
    val columnCount: Int,
)

/**
 * Lays sessions for a single room out on a timeline, splitting genuinely overlapping sessions
 * into side-by-side sub-columns (the same greedy-interval-coloring approach a calendar day view
 * uses) instead of letting one silently hide the other.
 */
private fun assignOverlapColumns(
    sessions: List<SessionDetails>,
    confDate: LocalDate,
): List<PlacedSession> {
    data class Interval(val session: SessionDetails, val start: Int, val end: Int)

    val sorted = sessions
        .map { Interval(it, it.startsAt.offsetMinutes(confDate), it.endsAt.offsetMinutes(confDate)) }
        .sortedWith(compareBy({ it.start }, { it.end }))

    // Group into clusters of transitively-overlapping sessions.
    val clusters = mutableListOf<MutableList<Interval>>()
    var clusterEnd = Int.MIN_VALUE
    for (interval in sorted) {
        if (clusters.isEmpty() || interval.start >= clusterEnd) {
            clusters += mutableListOf(interval)
            clusterEnd = interval.end
        } else {
            clusters.last() += interval
            clusterEnd = maxOf(clusterEnd, interval.end)
        }
    }

    val result = mutableListOf<PlacedSession>()
    for (cluster in clusters) {
        val columnEnds = mutableListOf<Int>()
        val columnIndexOf = mutableMapOf<Interval, Int>()
        for (interval in cluster) {
            val freeColumn = columnEnds.indexOfFirst { it <= interval.start }
            if (freeColumn >= 0) {
                columnEnds[freeColumn] = interval.end
                columnIndexOf[interval] = freeColumn
            } else {
                columnEnds += interval.end
                columnIndexOf[interval] = columnEnds.size - 1
            }
        }
        val columnCount = columnEnds.size
        cluster.forEach { interval ->
            result += PlacedSession(
                session = interval.session,
                startOffset = interval.start,
                endOffset = interval.end,
                columnIndex = columnIndexOf.getValue(interval),
                columnCount = columnCount,
            )
        }
    }
    return result
}

private fun LocalDateTime.offsetMinutes(referenceDate: LocalDate): Int {
    val dayDiff = (date.toEpochDays() - referenceDate.toEpochDays()).toInt()
    return dayDiff * 24 * 60 + hour * 60 + minute
}

private fun floorToSlot(minutes: Int, slot: Int) = (minutes / slot) * slot
private fun ceilToSlot(minutes: Int, slot: Int) = ((minutes + slot - 1) / slot) * slot

private fun formatMinuteOfDay(minutes: Int): String {
    val normalized = ((minutes % (24 * 60)) + 24 * 60) % (24 * 60)
    val hour = normalized / 60
    val minute = normalized % 60
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

@Composable
private fun SessionGridCard(
    conference: String,
    session: SessionDetails,
    bookmarks: Set<String>,
    height: Dp,
    sessionSelected: (id: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    val compact = height < CompactCardThreshold

    Surface(
        modifier = modifier
            .clickable(onClick = { sessionSelected(session.id) })
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary)),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = if (compact) 4.dp else 12.dp
                ),
            ) {
                Text(
                    text = session.title,
                    fontSize = if (compact) 12.sp else 16.sp,
                    maxLines = if (compact) 2 else 4,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                if (!compact) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Speakers(conference, session)
                }
                if (session.isLightning()) {
                    Surface(
                        modifier = Modifier.padding(top = if (compact) 2.dp else 8.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(Modifier.padding(vertical = 2.dp, horizontal = 6.dp)) {
                            Icon(
                                imageVector = ConfettiIcons.Bolt,
                                contentDescription = "lightning",
                                modifier = Modifier.size(12.dp),
                            )
                            if (!compact) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Lightning / ${session.startsAt.time}-${session.endsAt.time}",
                                    fontSize = 10.sp,
                                )
                            }
                        }
                    }
                }
            }
            Bookmark(
                modifier = Modifier.align(Alignment.TopEnd),
                compact = compact,
                bookmarks = bookmarks,
                session = session,
                isLoggedIn = isLoggedIn,
                removeBookmark = removeBookmark,
                addBookmark = addBookmark,
                onNavigateToSignIn = onNavigateToSignIn
            )
        }
    }
}

@Composable
private fun Speakers(conference: String, session: SessionDetails) {
    session.speakers.forEach { speaker ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val url = speaker.speakerDetails.avatarUrl()
            if (url?.isNotEmpty() == true) {
                AsyncImage(
                    model = url,
                    contentDescription = speaker.speakerDetails.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = speaker.speakerDetails.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

        }
    }
}

@Composable
private fun Bookmark(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    bookmarks: Set<String>,
    session: SessionDetails,
    isLoggedIn: Boolean,
    removeBookmark: (sessionId: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val isBookmarked = bookmarks.contains(session.id)
    if (isBookmarked) {
        val onClick = {
            if (isLoggedIn) {
                removeBookmark(session.id)
            } else {
                showDialog = true
            }
        }
        if (compact) {
            // A full IconButton enforces a 48dp minimum touch target, which doesn't fit a
            // lightning-talk-sized card - so trade that down for a small, directly-clickable icon.
            Icon(
                imageVector = ConfettiIcons.Bookmark,
                contentDescription = "remove bookmark",
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier
                    .padding(4.dp)
                    .size(14.dp)
                    .clickable(onClick = onClick)
            )
        } else {
            IconButton(
                modifier = modifier,
                onClick = onClick,
            ) {
                Icon(
                    imageVector = ConfettiIcons.Bookmark,
                    contentDescription = "remove bookmark",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    } else {
        // disable from this view for now
/*
        IconButton(
            modifier = modifier,
            onClick = {
                if (isLoggedIn) {
                    addBookmark(session.id)
                } else {
                    showDialog = true
                }
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkAdd,
                contentDescription = "add bookmark",
                modifier = Modifier.padding(8.dp)
            )
        }

 */
    }
    if (showDialog) {
        SignInDialog(
            onDismissRequest = { showDialog = false },
            onSignInClicked = onNavigateToSignIn
        )
    }
}

@MobilePreviews
@Composable
internal fun SessionListGridViewLoadedPreview() {
    SessionListGridView(
        uiState = sessionsSuccessState,
        sessionSelected = {},
        addBookmark = {},
        removeBookmark = {},
        onNavigateToSignIn = {},
        isLoggedIn = false,
        onRefresh = {},
    )
}

@Preview(name = "Loading", widthDp = 960, heightDp = 600, showBackground = true)
@Composable
internal fun SessionListGridViewLoadingPreview() {
    SessionListGridView(
        uiState = SessionsUiState.Loading,
        sessionSelected = {},
        addBookmark = {},
        removeBookmark = {},
        onNavigateToSignIn = {},
        isLoggedIn = false,
        onRefresh = {},
    )
}
