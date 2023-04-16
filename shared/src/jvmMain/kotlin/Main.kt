package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.di.initKoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

@Suppress("UNUSED_PARAMETER")
suspend fun main(args: Array<String>) {
    val koin = initKoin().koin
    val repo = koin.get<ConfettiRepository>()
    val clientCache = koin.get<ApolloClientCache>()

    try {
        withTimeout(60000L) {
            println("Sessions")
            val sessions = repo.sessionsQuery("droidconlondon2022").toFlow().first {
                // First emission is a cache miss, ignore it
                it.exception == null
            }
            sessions.data!!.sessions.nodes.map { it.sessionDetails }.forEach { session ->
                println("${session.startsAt} ${session.title}")
            }
        }
    } finally {
        println("done")
        clientCache.close()
    }
}
