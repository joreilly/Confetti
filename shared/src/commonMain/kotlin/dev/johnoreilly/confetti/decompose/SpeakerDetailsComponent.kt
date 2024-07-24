package dev.johnoreilly.confetti.decompose

import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState.Error
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState.Loading
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState.Success
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SpeakerDetailsComponent {

    val uiState: Value<SpeakerDetailsUiState>

    fun onSessionClicked(id: String)
    fun onCloseClicked()
}

sealed class SpeakerDetailsUiState {
    object Loading : SpeakerDetailsUiState()
    object Error : SpeakerDetailsUiState()
    class Success(val conference: String, val details: SpeakerDetails) : SpeakerDetailsUiState()
}

class DefaultSpeakerDetailsComponent(
    componentContext: ComponentContext,
    conference: String,
    speakerId: String,
    private val onSessionSelected: (id: String) -> Unit,
    private val onFinished: () -> Unit = {},
) : SpeakerDetailsComponent, KoinComponent, ComponentContext by componentContext {
    private val repository: ConfettiRepository by inject()
    private val coroutineScope = coroutineScope()

    override val uiState: Value<SpeakerDetailsUiState> = flow {
        // FixMe: add .speaker(id)
        val response = repository.conferenceData(conference = conference, FetchPolicy.CacheFirst)
        val details = response.data?.speakers?.nodes?.map { it.speakerDetails }
            ?.firstOrNull { it.id == speakerId }

        if (details != null) {
            emit(Success(conference, details))
        } else {
            emit(Error)
        }
    }.asValue(initialValue = Loading, lifecycle = lifecycle)

    override fun onSessionClicked(id: String) {
        onSessionSelected(id)
    }

    override fun onCloseClicked() {
        onFinished()
    }
}

