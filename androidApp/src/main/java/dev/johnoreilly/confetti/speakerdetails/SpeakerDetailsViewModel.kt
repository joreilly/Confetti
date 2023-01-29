package dev.johnoreilly.confetti.speakerdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.spakerdetails.navigation.SpeakerDetailsDestination
import kotlinx.coroutines.flow.*


class SpeakerDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository
) : ViewModel() {

    private val speakerId: String? = savedStateHandle[SpeakerDetailsDestination.speakerIdArg]

    val speaker: StateFlow<SpeakerDetails?> = repository.speakers.map {
        it.firstOrNull { it.id == speakerId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}