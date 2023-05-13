package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.SpeakerDetailsUiState.Error
import dev.johnoreilly.confetti.SpeakerDetailsUiState.Loading
import dev.johnoreilly.confetti.SpeakerDetailsUiState.Success
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.flow

interface SpeakerDetailsComponent {

    val uiState: Value<SpeakerDetailsUiState>

    fun onSessionClicked(id: String)
    fun onCloseClicked()
}

sealed class SpeakerDetailsUiState {
    object Loading : SpeakerDetailsUiState()
    object Error : SpeakerDetailsUiState()
    class Success(val details: SpeakerDetails) : SpeakerDetailsUiState()
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

    override val uiState: Value<SpeakerDetailsUiState> = flow {
        // FixMe: add .speaker(id)
        val response = repository.conferenceData(conference = conference, FetchPolicy.CacheFirst)
        val details = response.data?.speakers?.nodes?.map { it.speakerDetails }
            ?.firstOrNull { it.id == speakerId }

        if (details != null) {
            emit(Success(details))
        } else {
            emit(Error)
        }
    }.asValue(initialValue = Loading, scope = coroutineScope)

    override fun onSessionClicked(id: String) {
        onSessionSelected(id)
    }

    override fun onCloseClicked() {
        onFinished()
    }
}

