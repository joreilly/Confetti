package dev.johnoreilly.confetti.decompose

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.ConferencesComponent.Error
import dev.johnoreilly.confetti.decompose.ConferencesComponent.Loading
import dev.johnoreilly.confetti.decompose.ConferencesComponent.Success
import dev.johnoreilly.confetti.decompose.ConferencesComponent.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    class Success(val conferenceListByYear: Map<Int, List<GetConferencesQuery.Conference>>) : UiState {
        val relevantConferences: List<GetConferencesQuery.Conference> by lazy { conferenceListByYear.values.flatten() }
    }
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
        println("Fetching content with 2 sec delay")
        // simulate a delay
        delay(2000)
        println("emitted result")
        for (uiState in channel) {
            emit(uiState)
        }
    }.asValue(initialValue = Loading, lifecycle = lifecycle)

    override fun refresh() = refresh(false)

    private fun refresh(initial: Boolean) {
        job?.cancel()
        job = coroutineScope.launch {
            var hasConferences = false
            if (initial) {
                repository.conferences(FetchPolicy.CacheFirst).data?.conferences?.let {
                    hasConferences = true
                    val conferenceListByYear = it.groupBy { it.days[0].year }
                    channel.send(Success(groupConferencesByYear(it)))
                }
            }
            repository.conferences(FetchPolicy.NetworkOnly).data?.conferences?.let {
                hasConferences = true
                channel.send(Success(groupConferencesByYear(it)))
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