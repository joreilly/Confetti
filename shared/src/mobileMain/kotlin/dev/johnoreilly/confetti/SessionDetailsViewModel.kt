package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SessionDetailsComponent {

    val addErrorChannel: Channel<Int>
    val removeErrorChannel: Channel<Int>
    val uiState: Value<SessionDetailsUiState>
    val isBookmarked: StateFlow<Boolean>

    fun addBookmark()
    fun removeBookmark()
    fun onCloseClicked()
    fun onSignInClicked()
    fun onSpeakerClicked(id: String)
}

sealed class SessionDetailsUiState {
    object Loading : SessionDetailsUiState()
    object Error : SessionDetailsUiState()
    data class Success(val sessionDetails: SessionDetails) : SessionDetailsUiState()
}

class DefaultSessionDetailsComponent(
    componentContext: ComponentContext,
    private val conference: String,
    private val sessionId: String,
    private val user: User?,
    private val onFinished: () -> Unit,
    private val onSignIn: () -> Unit,
    private val onSpeakerSelected: (id: String) -> Unit,
) : SessionDetailsComponent, KoinComponent, ComponentContext by componentContext {
    private val repository: ConfettiRepository by inject()
    private val coroutineScope = coroutineScope()

    private var addErrorCount = 1
    private var removeErrorCount = 1
    override val addErrorChannel = Channel<Int>()
    override val removeErrorChannel = Channel<Int>()

    override val uiState: Value<SessionDetailsUiState> =
        repository.sessionDetails(conference = conference, sessionId = sessionId)
            .map {
                val details = it.data?.session?.sessionDetails
                if (details != null) {
                    SessionDetailsUiState.Success(details)
                } else {
                    SessionDetailsUiState.Error
                }
            }
            .asValue(initialValue = SessionDetailsUiState.Loading, lifecycle = lifecycle)

    override val isBookmarked: StateFlow<Boolean> = flow {
        val response =
            repository.bookmarks(conference, user?.uid, user, FetchPolicy.CacheFirst).first()

        fun GetBookmarksQuery.Bookmarks?.hasSessionId(): Boolean {
            return this?.sessionIds.orEmpty().toSet().contains(sessionId)
        }
        emitAll(
            repository.watchBookmarks(conference, user?.uid, user, response.data)
                .onStart { emit(response.data?.bookmarks.hasSessionId()) }
                .map { it.data?.bookmarks.hasSessionId() }
        )
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), false)

    override fun addBookmark() {
        coroutineScope.launch {
            val success = repository.addBookmark(conference, user?.uid, user, sessionId)
            if (!success) {
                addErrorChannel.send(addErrorCount++)
            }
        }
    }

    override fun removeBookmark() {
        coroutineScope.launch {
            val success = repository.removeBookmark(conference, user?.uid, user, sessionId)
            if (!success) {
                removeErrorChannel.send(removeErrorCount++)
            }
        }
    }

    override fun onCloseClicked() {
        onFinished()
    }

    override fun onSignInClicked() {
        onSignIn()
    }

    override fun onSpeakerClicked(id: String) {
        onSpeakerSelected(id)
    }
}
