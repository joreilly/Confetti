package dev.johnoreilly.confetti

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class JobConferenceRefresh(
    private val confettiRepository: ConfettiRepository
): ConferenceRefresh {
    val coroutineScope: CoroutineScope = MainScope()

    private var refreshJob: Job? = null

    override fun refresh(conference: String) {
        refreshJob?.cancel()

        if (conference.isNotEmpty()) {
            refreshJob = coroutineScope.launch {
                confettiRepository.refresh(networkOnly = false)
            }
        }
    }
}