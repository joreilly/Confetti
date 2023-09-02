package dev.johnoreilly.confetti.wear.conferences

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.asValue
import dev.johnoreilly.confetti.decompose.coroutineScope
import dev.johnoreilly.confetti.wear.conferences.ConferencesComponent.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface ConferencesComponent {
    val uiState: Value<UiState>

    fun refresh()
    fun onConferenceClicked(conference: GetConferencesQuery.Conference)
    sealed interface UiState
    object Loading : UiState
    object Error : UiState
    class Success(val conferenceList: List<GetConferencesQuery.Conference>) : UiState
}

class DefaultConferencesComponent(
    componentContext: ComponentContext,
    private val onConferenceSelected: (conference: GetConferencesQuery.Conference) -> Unit,
) : ConferencesComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()
    val repository: ConfettiRepository = get()

    private var job: Job? = null

    private val channel = Channel<UiState>()

    init {
        refresh(true)
    }

    override val uiState: Value<UiState> = flow {
        for (uiState in channel) {
            emit(uiState)
        }
    }.asValue(initialValue = ConferencesComponent.Loading, lifecycle = lifecycle)

    override fun refresh() = refresh(false)

    private fun refresh(initial: Boolean) {
        job?.cancel()
        job = coroutineScope.launch {
            var hasConferences = false
            if (initial) {
                repository.conferences(FetchPolicy.CacheFirst).data?.conferences?.let {
                    hasConferences = true
                    channel.send(ConferencesComponent.Success(it))
                }
            }
            repository.conferences(FetchPolicy.NetworkOnly).data?.conferences?.let {
                hasConferences = true
                channel.send(ConferencesComponent.Success(it))
            }

            if (!hasConferences) {
                channel.send(ConferencesComponent.Error)
            }
        }
    }

    override fun onConferenceClicked(conference: GetConferencesQuery.Conference) {
        onConferenceSelected(conference)
    }
}
