package dev.johnoreilly.confetti.wear.speakerdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.wear.speakerdetails.navigation.SpeakerDetailsDestination
import kotlinx.coroutines.flow.*


class SpeakerDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository
) : ViewModel() {

    private val speakerId: String? = savedStateHandle[SpeakerDetailsDestination.speakerIdArg]
    private val conference: String? = savedStateHandle[SpeakerDetailsDestination.conferenceArg]

    val speaker: StateFlow<SpeakerDetails?> = flow {
        if (speakerId != null && conference != null) {
            // FixMe: add .speaker(id)
            val response = repository.conferenceData(conference = conference, FetchPolicy.CacheOnly)
            emit(response.data?.speakers?.map { it.speakerDetails }?.firstOrNull { it.id == speakerId })
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

}