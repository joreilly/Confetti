package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.stateIn
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class SpeakersViewModel : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()

    private lateinit var conference: String

    // FIXME: can we pass that as a parameter somehow
    fun configure(conference: String) {
        this.conference = conference
    }

    val speakers: StateFlow<SpeakersUiState> = flow {
        repository.conferenceData(conference, FetchPolicy.CacheOnly)
            .data?.speakers?.map {
            it.speakerDetails
        }.let {
            it?.let {
                emit(SpeakersUiState.Success(conference, it))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpeakersUiState.Loading)
}

sealed interface SpeakersUiState {
    object Loading : SpeakersUiState
    object Error : SpeakersUiState

    data class Success(
        val conference: String,
        val speakers: List<SpeakerDetails>,
    ) : SpeakersUiState
}
