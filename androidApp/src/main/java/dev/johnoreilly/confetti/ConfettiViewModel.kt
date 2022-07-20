package dev.johnoreilly.confetti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*


class ConfettiViewModel(private val repository: ConfettiRepository) : ViewModel() {
    val enabledLanguages: Flow<Set<String>> = repository.enabledLanguages

    val sessions = Pager(PagingConfig(pageSize = 10)) {
        SessionPagingSource(repository)
    }.flow
        .map { pagingData ->
            pagingData.filter {
                val filterFavorites = repository.filterFavoriteSessions.value
                !filterFavorites || it.node.sessionDetails.isFavorite
            }
        }
        .cachedIn(viewModelScope)

    val speakers = repository.speakers
    val rooms = repository.rooms

    val filterFavoriteSessions = repository.filterFavoriteSessions as Flow<Boolean>

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

    fun fetchMoreSessions() {
        repository.fetchMoreSessions()
    }

    fun setSessionFavorite(sessionId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.setSessionFavorite(sessionId, isFavorite)
        }
    }

    fun onFavoriteFilterClick() {
        repository.filterFavoriteSessions.value = !repository.filterFavoriteSessions.value
    }
}

private class SessionPagingSource(
    private val repository: ConfettiRepository
) : PagingSource<String, GetSessionsQuery.Edge>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, GetSessionsQuery.Edge> {
        val cursor = params.key
        return try {
            val sessionEdges = repository.fetchSessionPage(cursor)
            LoadResult.Page(
                data = sessionEdges,
                prevKey = null,
                nextKey = sessionEdges.lastOrNull()?.cursor
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, GetSessionsQuery.Edge>): String? {
        return null
    }
}
