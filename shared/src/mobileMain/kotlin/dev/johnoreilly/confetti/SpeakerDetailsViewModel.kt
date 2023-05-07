package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import dev.johnoreilly.confetti.SpeakerDetailsComponent.Error
import dev.johnoreilly.confetti.SpeakerDetailsComponent.Loading
import dev.johnoreilly.confetti.SpeakerDetailsComponent.Success
import dev.johnoreilly.confetti.SpeakerDetailsComponent.UiState
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

interface SpeakerDetailsComponent {

    val speaker: StateFlow<UiState>

    fun onSessionClicked(id: String)
    fun onCloseClicked()

    sealed interface UiState
    object Loading : UiState
    object Error : UiState
    class Success(val details: SpeakerDetails) : UiState
}

class DefaultSpeakerDetailsComponent(
    componentContext: ComponentContext,
    repository: ConfettiRepository,
    conference: String,
    speakerId: String,
    private val onSessionSelected: (id: String) -> Unit,
    private val onFinished: () -> Unit,
) : SpeakerDetailsComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    override val speaker: StateFlow<UiState> = flow {
        // FixMe: add .speaker(id)
        val response = repository.conferenceData(conference = conference, FetchPolicy.CacheFirst)
        val details = response.data?.speakers?.nodes?.map { it.speakerDetails }
            ?.firstOrNull { it.id == speakerId }

        if (details != null) {
            emit(Success(details))
        } else {
            emit(Error)
        }
    }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), Loading)

    override fun onSessionClicked(id: String) {
        onSessionSelected(id)
    }

    override fun onCloseClicked() {
        onFinished()
    }
}

