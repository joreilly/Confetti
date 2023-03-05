package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.di.initKoin
import kotlinx.coroutines.flow.first

suspend fun main() {
    val koin = initKoin().koin
    val repo = koin.get<ConfettiRepository>()
    repo.setConference("droidconlondon2022")

    println("Sessions")
    val sessions = repo.sessions.first()
    sessions.forEach { session ->
        println("${session.startInstant} ${session.title}")
    }
}