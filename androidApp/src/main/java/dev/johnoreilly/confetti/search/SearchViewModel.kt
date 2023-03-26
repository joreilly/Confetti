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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

class SearchViewModel(
    private val sessionsViewModel: SessionsViewModel,
    private val speakersViewModel: SpeakersViewModel,
) : ViewModel() {

    val search = MutableStateFlow("")

    val sessions = sessionsViewModel
        .uiState
        .mapNotNull { state ->
            if (state is SessionsUiState.Success) {
                state.sessionsByStartTimeList.flatMap { it.values }.flatten()
            } else {
                null
            }
        }
        .combine(search) { sessions, search ->
            sessions.filter { filterSessions(it, search) }
        }

    val speakers = speakersViewModel
        .speakers
        .mapNotNull { state ->
            if (state is SpeakersUiState.Success) {
                state.speakers
            } else {
                null
            }
        }
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
}
