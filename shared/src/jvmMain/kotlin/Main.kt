import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.ConfettiRepository

suspend fun main() {
    val koin = initKoin().koin
    val repo = koin.get<ConfettiRepository>()
    repo.setConference("droidconlondon2022")

    println("Sessions")
    repo.sessions.collect { sessions ->
        sessions.forEach { session ->
            println("${session.startInstant} ${session.title}")
        }
    }
}