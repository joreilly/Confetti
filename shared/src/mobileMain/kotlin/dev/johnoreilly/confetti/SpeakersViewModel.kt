package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.stateIn
import dev.johnoreilly.confetti.AppSettings.Companion.CONFERENCE_NOT_SET
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class SpeakersViewModel : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()

    private var conference: String = CONFERENCE_NOT_SET

    // FIXME: can we pass that as a parameter somehow
    fun setConference(conference: String) {
        this.conference = conference
    }

    val speakers: StateFlow<SpeakersUiState> = flow {
        repository.conferenceData(conference, FetchPolicy.CacheOnly).also {
            println(it.errors)
            println(it.exception)
        }
            .data?.speakers?.map {
            it.speakerDetails
        }.let {
            it?.let {
                emit(SpeakersUiState.Success(it))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpeakersUiState.Loading)
}

sealed interface SpeakersUiState {
    object Loading : SpeakersUiState
    object Error : SpeakersUiState

    data class Success(
        val speakers: List<SpeakerDetails>,
    ) : SpeakersUiState
}
