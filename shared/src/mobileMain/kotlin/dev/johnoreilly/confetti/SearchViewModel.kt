package dev.johnoreilly.confetti

import com.rickclephas.kmm.viewmodel.KMMViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class SearchViewModel(
    // TODO: Remove dependency between view models.
    private val sessionsViewModel: SessionsViewModel,
    private val speakersViewModel: SpeakersViewModel,
) : KMMViewModel() {

    private val _search = MutableStateFlow("")
    val search: Flow<String> = _search.asStateFlow()

    val loading = sessionsViewModel
        .uiState
        .combine(speakersViewModel.speakers) { sessions, speakers ->
            sessions is SessionsUiState.Loading || speakers is SpeakersUiState.Loading
        }

    private val successSessions = sessionsViewModel
        .uiState
        .filterIsInstance<SessionsUiState.Success>()

    private val successSpeakers = speakersViewModel
        .speakers
        .filterIsInstance<SpeakersUiState.Success>()

    val sessions = successSessions
        .map { state ->
            state
                .sessionsByStartTimeList
                .flatMap { sessions -> sessions.values }
                .flatten()
        }
        .combine(search) { sessions, search ->
            sessions.filter { filterSessions(it, search) }
        }

    val bookmarks = successSessions
        .map { state -> state.bookmarks }

    val speakers = successSpeakers
        .map { state -> state.speakers }
        .combine(search) { sessions, search ->
            sessions.filter { filterSpeakers(it, search) }
        }

    private fun filterSpeakers(details: SpeakerDetails, filter: String): Boolean {
        if (filter.isBlank()) return false

        val ignoreCase = true
        return details.name.contains(filter, ignoreCase) ||
            details.bio.orEmpty().contains(filter, ignoreCase) ||
            details.city.orEmpty().contains(filter, ignoreCase) ||
            details.company.orEmpty().contains(filter, ignoreCase)
    }

    private fun filterSessions(details: SessionDetails, filter: String): Boolean {
        if (filter.isBlank()) return false

        val ignoreCase = true
        return details.title.contains(filter, ignoreCase) ||
            details.sessionDescription.orEmpty().contains(filter, ignoreCase) ||
            details.room?.name.orEmpty().contains(filter, ignoreCase) ||
            details.speakers.any { speaker ->
                speaker.speakerDetails.name.contains(filter, ignoreCase)
            }
    }

    fun configure(conference: String) {
        sessionsViewModel.configure(conference)
        speakersViewModel.configure(conference)
    }

    fun onSearchChange(query: String) {
        _search.update { query }
    }

    fun addBookmark(sessionId: String) {
        sessionsViewModel.addBookmark(sessionId)
    }

    fun removeBookmark(sessionId: String) {
        sessionsViewModel.removeBookmark(sessionId)
    }
}
