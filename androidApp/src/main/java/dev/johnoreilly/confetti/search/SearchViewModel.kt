package dev.johnoreilly.confetti.search

import androidx.lifecycle.ViewModel
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.SessionsViewModel
import dev.johnoreilly.confetti.SpeakersUiState
import dev.johnoreilly.confetti.SpeakersViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class SearchViewModel(
    private val sessionsViewModel: SessionsViewModel,
    private val speakersViewModel: SpeakersViewModel,
) : ViewModel() {

    val search = MutableStateFlow("")

    private val sessionsState = sessionsViewModel
        .uiState
        .filterIsInstance<SessionsUiState.Success>()

    private val speakersState = speakersViewModel
        .speakers
        .filterIsInstance<SpeakersUiState.Success>()

    val sessions = sessionsState
        .map { state ->
            state
                .sessionsByStartTimeList
                .flatMap { sessions -> sessions.values }
                .flatten()
        }
        .combine(search) { sessions, search ->
            sessions.filter { filterSessions(it, search) }
        }

    val bookmarks = sessionsState
        .map { state -> state.bookmarks }

    val speakers = speakersState
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
        search.update { query }
    }

    fun addBookmark(sessionId: String) {
        sessionsViewModel.addBookmark(sessionId)
    }

    fun removeBookmark(sessionId: String) {
        sessionsViewModel.removeBookmark(sessionId)
    }
}
