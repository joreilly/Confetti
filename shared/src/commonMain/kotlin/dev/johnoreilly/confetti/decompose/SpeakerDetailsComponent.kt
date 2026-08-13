package dev.johnoreilly.confetti.decompose

import com.apollographql.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState.Error
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState.Loading
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState.Success
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SpeakerDetailsComponent {

    val uiState: Value<SpeakerDetailsUiState>
    val isLoggedIn: Boolean

    fun addBookmark(sessionId: String)
    fun removeBookmark(sessionId: String)
    fun onSessionClicked(id: String)
    fun onCloseClicked()
    fun onSignInClicked()
}

sealed class SpeakerDetailsUiState {
    data object Loading : SpeakerDetailsUiState()
    data object Error : SpeakerDetailsUiState()
    data class Success(
        val conference: String,
        val details: SpeakerDetails,
        val bookmarks: Set<String> = emptySet(),
    ) : SpeakerDetailsUiState()
}

class DefaultSpeakerDetailsComponent(
    componentContext: ComponentContext,
    private val conference: String,
    private val speakerId: String,
    private val user: User? = null,
    private val onSessionSelected: (id: String) -> Unit,
    private val onSignIn: () -> Unit = {},
    private val onFinished: () -> Unit = {},
) : SpeakerDetailsComponent, KoinComponent, ComponentContext by componentContext {
    private val repository: ConfettiRepository by inject()
    private val coroutineScope = coroutineScope()

    override val isLoggedIn: Boolean = user != null

    override val uiState: Value<SpeakerDetailsUiState> = flow {
        val initialBookmarks = repository.bookmarks(conference, user?.uid, user, FetchPolicy.CacheFirst).first()
        // FixMe: add .speaker(id)
        val response = repository.conferenceData(conference = conference, FetchPolicy.CacheFirst)
        val details = response.data?.speakers?.nodes?.map { it.speakerDetails }
            ?.firstOrNull { it.id == speakerId }

        if (details != null) {
            val initialBookmarksSet = initialBookmarks.data?.bookmarks?.sessionIds.orEmpty().toSet()
            emitAll(
                repository.watchBookmarks(conference, user?.uid, user, initialBookmarks.data)
                    .map { it.data?.bookmarks?.sessionIds.orEmpty().toSet() }
                    .onStart { emit(initialBookmarksSet) }
                    .map { bookmarks ->
                        Success(conference, details, bookmarks)
                    }
            )
        } else {
            emit(Error)
        }
    }.asValue(initialValue = Loading, lifecycle = lifecycle)

    override fun addBookmark(sessionId: String) {
        coroutineScope.launch {
            repository.addBookmark(conference, user?.uid, user, sessionId)
        }
    }

    override fun removeBookmark(sessionId: String) {
        coroutineScope.launch {
            repository.removeBookmark(conference, user?.uid, user, sessionId)
        }
    }

    override fun onSessionClicked(id: String) {
        onSessionSelected(id)
    }

    override fun onCloseClicked() {
        onFinished()
    }

    override fun onSignInClicked() {
        onSignIn()
    }
}


