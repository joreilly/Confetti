package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.coroutineScope
import com.rickclephas.kmm.viewmodel.stateIn
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SessionDetailsViewModel(
    private val repository: ConfettiRepository
) : KMMViewModel() {
    fun configure(
        conference: String,
        sessionId: String,
        uid: String?,
        tokenProvider: TokenProvider?,
    ) {
        this.conference = conference
        this.sessionId = sessionId
        this.uid = uid
        this.tokenProvider = tokenProvider
    }

    private lateinit var sessionId: String
    private lateinit var conference: String
    private var uid: String? = null
    private var tokenProvider: TokenProvider? = null

    private var addErrorCount = 1
    private var removeErrorCount = 1
    val addErrorChannel = Channel<Int>()
    val removeErrorChannel = Channel<Int>()

    val session: StateFlow<SessionDetails?> = flow {
        emitAll(repository.sessionDetails(conference = conference, sessionId = sessionId)
            .map { it.data?.session?.sessionDetails })
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isBookmarked =  flow {
        val response = repository.bookmarks(conference, uid, tokenProvider, FetchPolicy.CacheFirst).first()

        fun GetBookmarksQuery.Bookmarks?.hasSessionId(): Boolean {
            return this?.sessionIds.orEmpty().toSet().contains(sessionId)
        }
        emitAll(
            repository.watchBookmarks(conference, uid, tokenProvider, response.data)
                .onStart { emit(response.data?.bookmarks.hasSessionId()) }
                .map { it.data?.bookmarks.hasSessionId() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addBookmark() {
        viewModelScope.coroutineScope.launch {
            val success = repository.addBookmark(conference, uid, tokenProvider, sessionId)
            if (!success) {
                addErrorChannel.send(addErrorCount++)
            }
        }
    }

    fun removeBookmark() {
        viewModelScope.coroutineScope.launch {
            val success = repository.removeBookmark(conference, uid, tokenProvider, sessionId)
            if (!success) {
                removeErrorChannel.send(removeErrorCount++)
            }
        }
    }
}
