package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface SpeakersComponent {
    val uiState: Value<SpeakersUiState>

    fun onSpeakerClicked(id: String)
}

class DefaultSpeakersComponent(
    componentContext: ComponentContext,
    conference: String,
    private val onSpeakerSelected: (id: String) -> Unit,
) : SpeakersComponent, KoinComponent, ComponentContext by componentContext {
    private val simpleComponent =
        SpeakersSimpleComponent(
            componentContext = childContext(key = "Sessions"),
            repository = get(),
            conference = conference,
        )

    override val uiState: Value<SpeakersUiState> =
        simpleComponent.speakers.asValue(lifecycle = lifecycle)

    override fun onSpeakerClicked(id: String) {
        onSpeakerSelected(id)
    }
}

class SpeakersSimpleComponent(
    componentContext: ComponentContext,
    repository: ConfettiRepository,
    conference: String,
) : ComponentContext by componentContext, KoinComponent {

    private val coroutineScope = coroutineScope()

    val speakers: StateFlow<SpeakersUiState> = flow {
        repository.conferenceData(conference, FetchPolicy.CacheFirst)
            .data?.speakers?.nodes?.map {
                it.speakerDetails
            }.let {
                it?.let {
                    emit(SpeakersUiState.Success(conference, it))
                }
            }
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), SpeakersUiState.Loading)
}

sealed interface SpeakersUiState {
    object Loading : SpeakersUiState
    object Error : SpeakersUiState

    data class Success(
        val conference: String,
        val speakers: List<SpeakerDetails>,
    ) : SpeakersUiState
}
