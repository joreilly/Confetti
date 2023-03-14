package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.di.initKoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) = runBlocking {
    val koin = initKoin().koin
    val repo = koin.get<ConfettiRepository>()
    val clientCache = koin.get<ApolloClientCache>()

    repo.setConference("droidconlondon2022")
    launch {
        repo.initOnce()
    }

    println("Sessions")
    val sessions = repo.sessions.first()
    sessions.forEach { session ->
        println("${session.startInstant} ${session.title}")
    }
    println("done")
    clientCache.close()
}