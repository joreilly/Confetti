import dev.johnoreilly.confetti.ConfettiRepository

suspend fun main() {
    val repo = ConfettiRepository()

    repo.sessions.collect { sessions ->

        val result = sessions.groupBy { it.start.date }.map { (_, sessions) ->
            sessions.groupBy { it.start }
        }


        println(result)
    }
}
























