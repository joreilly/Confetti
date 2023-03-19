package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.stateIn
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.*


class SpeakerDetailsViewModel(
    repository: ConfettiRepository
) : KMMViewModel() {
    sealed interface UiState

    object Loading: UiState
    object Error: UiState
    class Success(val details: SpeakerDetails): UiState
    private lateinit var speakerId: String
    private lateinit var conference: String

    fun configure(conference: String, speakerId: String) {
        this.conference = conference
        this.speakerId = speakerId
    }

    val speaker: StateFlow<UiState> = flow {
        // FixMe: add .speaker(id)
        val response = repository.conferenceData(conference = conference, FetchPolicy.CacheOnly)
        val details = response.data?.speakers?.map { it.speakerDetails }?.firstOrNull { it.id == speakerId }

        if (details != null) {
            emit(Success(details))
        } else {
            emit(Error)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

}

