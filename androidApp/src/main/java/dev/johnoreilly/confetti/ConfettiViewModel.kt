package dev.johnoreilly.confetti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.*


class ConfettiViewModel(private val repository: ConfettiRepository) : ViewModel() {
    val enabledLanguages: Flow<Set<String>> = repository.enabledLanguages

    private val _filterFavoriteSessions: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val filterFavoriteSessions: Flow<Boolean> = _filterFavoriteSessions

    @OptIn(ExperimentalPagingApi::class)
    val sessions = Pager(
        config = PagingConfig(pageSize = 10),
        remoteMediator = SessionRemoteMediator(repository)
    ) {
        SessionPagingSource(repository)
    }.flow
        .map { pagingData ->
            pagingData.filter {
                val filterFavorites = _filterFavoriteSessions.value
                !filterFavorites || it.node.sessionDetails.isFavorite
            }
        }

    val speakers = repository.speakers
    val rooms = repository.rooms

    fun getSession(sessionId: String): Flow<SessionDetails?> {
        return repository.getSession(sessionId)
    }

    fun onLanguageChecked(language: String, checked: Boolean) {
        repository.updateEnableLanguageSetting(language, checked)
    }

    fun getSessionSpeakerLocation(session: SessionDetails): String {
        var text = session.speakers.joinToString(", ") { it.name }
        text += " / ${session.room.name} / ${getLanguageInEmoji(session.language)}"
        return text
    }

    fun getSessionTime(session: SessionDetails): String {
        return repository.getSessionTime(session)
    }

    fun getLanguageInEmoji(language: String?): String {
        // TODO need to figure out how we want to generally handle languages
        return when (language?.lowercase(Locale.ROOT)) {
            "english" -> "\uD83C\uDDEC\uD83C\uDDE7"
            "french" -> "\uD83C\uDDEB\uD83C\uDDF7"
            else -> ""
        }
    }

    fun setSessionFavorite(sessionId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.setSessionFavorite(sessionId, isFavorite)
        }
    }

    fun onFavoriteFilterClick() {
        _filterFavoriteSessions.value = !_filterFavoriteSessions.value
    }

    inner class SessionPagingSource(
        private val repository: ConfettiRepository
    ) : PagingSource<String, GetSessionsQuery.Edge>() {
        override suspend fun load(params: LoadParams<String>): LoadResult<String, GetSessionsQuery.Edge> {
            // Get all sessions from the cache, but we wantonly the page after the
            // given key: we slice the list manually.
            val allSessionEdges = repository.getAllSessionsFromCache()
            val indexOfCursor = allSessionEdges.indexOfFirst { it.cursor == params.key }
            val sessionEdgesAfterCursor =
                if (indexOfCursor == -1) {
                    allSessionEdges
                } else {
                    allSessionEdges.subList(indexOfCursor + 1, allSessionEdges.size)
                }

            // Observe the list to know when to invalidate this source
            viewModelScope.launch {
                repository.sessionsCacheInvalidated.take(1).collect {
                    invalidate()
                }
            }

            val nextKey = sessionEdgesAfterCursor.lastOrNull()?.cursor
            return LoadResult.Page(
                data = sessionEdgesAfterCursor,
                prevKey = null,
                nextKey = nextKey
            )
        }

        override fun getRefreshKey(state: PagingState<String, GetSessionsQuery.Edge>): String? {
            return null
        }

        override val keyReuseSupported = true
    }
}

@OptIn(ExperimentalPagingApi::class)
private class SessionRemoteMediator(
    private val repository: ConfettiRepository
) : RemoteMediator<String, GetSessionsQuery.Edge>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<String, GetSessionsQuery.Edge>
    ): MediatorResult {
        return try {
            val afterCursor: String? = when (loadType) {
                LoadType.REFRESH -> {
                    // null means get the first page
                    null
                }
                LoadType.PREPEND -> {
                    // Prepend not supported by API
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        // Can be null when no pages have been loaded yet
                        return MediatorResult.Success(endOfPaginationReached = false)
                    }
                    lastItem.cursor
                }
            }

            if (loadType == LoadType.REFRESH) {
                repository.clearCache()
            }

            val endOfPaginationReached = repository.fetchMoreSessions(after = afterCursor)
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
