import dev.johnoreilly.confetti.ConfettiRepository


suspend fun main() {
    val repo = ConfettiRepository()

    repo.sessions.collect { sessions ->
        sessions.forEach { session ->
            println("${session.startInstant}  ${session.title}")
        }
    }
}