package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.di.initKoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) = runBlocking {
    val koin = initKoin().koin
    val repo = koin.get<ConfettiRepository>()
    val clientCache = koin.get<ApolloClientCache>()

    println("Sessions")
    val sessions = repo.sessions("droidconlondon2022").first {
        // First emission is a cache miss, ignore it
        it.exception == null
    }
    sessions.data!!.sessions.nodes.map { it.sessionDetails }.forEach { session ->
        println("${session.startsAt} ${session.title}")
    }
    println("done")
    clientCache.close()
}
