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

open class SpeakersViewModel(private val conference: String) : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()

    val speakers: StateFlow<SpeakersUiState> = flow {
        repository.conferenceData(conference, FetchPolicy.CacheFirst)
            .data?.speakers?.nodes?.map {
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
