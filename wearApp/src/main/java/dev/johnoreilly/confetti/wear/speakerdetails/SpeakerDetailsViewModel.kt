package dev.johnoreilly.confetti.wear.speakerdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.ClientQuery.toUiState
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.speakerdetails.navigation.SpeakerDetailsDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class SpeakerDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository
) : ViewModel() {

    private val speakerId: String = savedStateHandle[SpeakerDetailsDestination.speakerIdArg]!!
    private val conference: String = savedStateHandle[SpeakerDetailsDestination.conferenceArg]!!

    val speaker: StateFlow<QueryResult<SpeakerDetails>> =
        repository.speakerQuery(conference = conference, id = speakerId).toUiState { data ->
            data.speaker.speakerDetails
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QueryResult.Loading)

}
