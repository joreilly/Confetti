import dev.johnoreilly.confetti.ConfettiRepository








// Having a JVM target also allows us to have Compose for Desktop client
suspend fun main() {
    val repo = ConfettiRepository()

    repo.sessions.collect { sessions ->
        sessions.forEach { session ->
            println("${session.start}  ${session.title}")
        }
    }
}
































































