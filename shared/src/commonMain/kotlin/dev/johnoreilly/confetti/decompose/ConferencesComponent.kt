package dev.johnoreilly.confetti.decompose

import com.apollographql.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.ConferencesComponent.Error
import dev.johnoreilly.confetti.decompose.ConferencesComponent.Loading
import dev.johnoreilly.confetti.decompose.ConferencesComponent.Success
import dev.johnoreilly.confetti.decompose.ConferencesComponent.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface ConferencesComponent {

    val uiState: Value<UiState>
    val onBack: (() -> Unit)?

    fun refresh()
    fun onConferenceClicked(conference: GetConferencesQuery.Conference)

    sealed interface UiState
    object Loading : UiState
    object Error : UiState
    class Success(
        val conferenceListByYear: Map<Int, List<GetConferencesQuery.Conference>>,
        val currentConference: String? = null
    ) : UiState {
        val relevantConferences: List<GetConferencesQuery.Conference> by lazy { conferenceListByYear.values.flatten() }
    }
}

class DefaultConferencesComponent(
    componentContext: ComponentContext,
    override val onBack: (() -> Unit)? = null,
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
    }.asValue(initialValue = Loading, lifecycle = lifecycle)

    override fun refresh() = refresh(false)

    private fun refresh(initial: Boolean) {
        job?.cancel()
        job = coroutineScope.launch {
            val currentConference = repository.getConference().takeIf { it != AppSettings.CONFERENCE_NOT_SET }
            var hasConferences = false
            if (initial) {
                repository.conferences(FetchPolicy.CacheFirst).data?.conferences?.let {
                    hasConferences = true
                    channel.send(Success(groupConferencesByYear(it), currentConference))
                }
            }
            repository.conferences(FetchPolicy.NetworkOnly).data?.conferences?.let {
                hasConferences = true
                channel.send(Success(groupConferencesByYear(it), currentConference))
            }

            if (!hasConferences) {
                channel.send(Error)
            }
        }
    }

    private fun groupConferencesByYear(conferences: List<GetConferencesQuery.Conference>): Map<Int, List<GetConferencesQuery.Conference>> {
        return conferences.groupBy { it.days[0].year }
    }

    override fun onConferenceClicked(conference: GetConferencesQuery.Conference) {
        onConferenceSelected(conference)
    }
}