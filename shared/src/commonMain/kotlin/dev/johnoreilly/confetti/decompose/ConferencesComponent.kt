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
import kotlinx.coroutines.delay
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

    private var showingError = false

    private fun refresh(initial: Boolean) {
        job?.cancel()
        job = coroutineScope.launch {
            // Retrying from the error screen: show progress rather than leaving the error up
            if (showingError) channel.send(Loading)
            showingError = false

            val currentConference = repository.getConference().takeIf { it != AppSettings.CONFERENCE_NOT_SET }
            var hasConferences = false
            if (initial) {
                // CacheOnly, not CacheFirst: on a cache miss CacheFirst would go to the network
                // too, doubling up with the fetch below
                repository.conferences(FetchPolicy.CacheOnly).data?.conferences?.let {
                    hasConferences = true
                    channel.send(Success(groupConferencesByYear(it), currentConference))
                }
            }
            fetchConferencesFromNetwork()?.let {
                hasConferences = true
                channel.send(Success(groupConferencesByYear(it), currentConference))
            }

            if (!hasConferences) {
                showingError = true
                channel.send(Error)
            }
        }
    }

    /**
     * A fresh install has nothing cached, so one failed request would leave the error screen up.
     * Retry a couple of times with a short back-off first: connections dropped during the TLS
     * handshake are usually transient.
     */
    private suspend fun fetchConferencesFromNetwork(): List<GetConferencesQuery.Conference>? {
        repeat(NETWORK_ATTEMPTS) { attempt ->
            val response = repository.conferences(FetchPolicy.NetworkOnly)
            response.data?.conferences?.let { return it }
            println("Fetching conferences failed (attempt ${attempt + 1} of $NETWORK_ATTEMPTS): ${response.exception ?: response.errors}")
            if (attempt < NETWORK_ATTEMPTS - 1) delay(RETRY_BASE_DELAY_MS * (attempt + 1))
        }
        return null
    }

    private fun groupConferencesByYear(conferences: List<GetConferencesQuery.Conference>): Map<Int, List<GetConferencesQuery.Conference>> {
        return conferences.groupBy { it.days[0].year }
    }

    override fun onConferenceClicked(conference: GetConferencesQuery.Conference) {
        onConferenceSelected(conference)
    }

    private companion object {
        const val NETWORK_ATTEMPTS = 3
        const val RETRY_BASE_DELAY_MS = 1_000L
    }
}