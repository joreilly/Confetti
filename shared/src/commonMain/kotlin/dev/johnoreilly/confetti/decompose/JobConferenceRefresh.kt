package dev.johnoreilly.confetti.decompose

import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.work.updateCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class JobConferenceRefresh : KoinComponent {
    private val coroutineScope: CoroutineScope = MainScope()

    private val confettiRepository = get<ConfettiRepository>()
    private val apolloClientCache = get<ApolloClientCache>()

    private var refreshJob: Job? = null

    fun refresh() {
        refreshJob?.cancel()
        coroutineScope.launch {
            updateCache(
                true,
                fetchImages = false,
                conference = confettiRepository.getConferenceFlow().first(),
                apolloClientCache = apolloClientCache,
                cacheImages = null
            )
        }
    }
}