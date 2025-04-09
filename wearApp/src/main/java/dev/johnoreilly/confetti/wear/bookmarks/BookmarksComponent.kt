package dev.johnoreilly.confetti.wear.bookmarks

import com.apollographql.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetBookmarkedSessionsQuery
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.coroutineScope
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.utils.ClientQuery.toUiState
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.utils.nowAtTimeZone
import dev.johnoreilly.confetti.wear.complication.ComplicationUpdater
import dev.johnoreilly.confetti.wear.tile.TileUpdater
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface BookmarksComponent {
    val uiState: StateFlow<QueryResult<BookmarksUiState>>

    fun onSessionClicked(session: String)
}

class DefaultBookmarksComponent(
    componentContext: ComponentContext,
    conference: String,
    user: User?,
    private val onSessionSelected: (String) -> Unit,
) : BookmarksComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    val repository: ConfettiRepository by inject()
    private val tileUpdater: TileUpdater by inject()
    private val complicationUpdater: ComplicationUpdater by inject()

    override val uiState: StateFlow<QueryResult<BookmarksUiState>> =
        repository.bookmarkedSessionsQuery(conference, user?.uid, user, FetchPolicy.NetworkFirst)
            .toUiState {
                it.toUiState()
            }.onEach {
                if (it is QueryResult.Success && it.cacheInfo?.isCacheHit != true) {
                    tileUpdater.updateAll()
                    complicationUpdater.update()
                }
            }.stateIn(coroutineScope, SharingStarted.Lazily, QueryResult.Loading)


    override fun onSessionClicked(session: String) {
        onSessionSelected(session)
    }

    companion object {
        fun GetBookmarkedSessionsQuery.Data.toUiState(): BookmarksUiState {
            val allSessions =
                this.bookmarkConnection?.nodes
                    ?.map { it.sessionDetails }
                    ?.sortedBy { it.startsAt }
                    .orEmpty()

            val timeZone = this.config.timezone.toTimeZone()

            val now = LocalDateTime.nowAtTimeZone(timeZone)

            val (upcoming, past) = allSessions.partition {
                it.endsAt > now
            }

            return BookmarksUiState(this.config.id, upcoming, past, now)
        }
    }
}
