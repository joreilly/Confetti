import dev.johnoreilly.confetti.ConfettiRepository
import java.io.File

suspend fun main() {
//    // to workaround https://github.com/apollographql/apollo-kotlin/issues/3758 for now
//    val file = File("confetti.db")
//    val result = file.delete()

    val repo = ConfettiRepository()

    println("Sessions")
    repo.sessions.collect { sessions ->
        sessions.forEach { session ->
            println("${session.startInstant}  ${session.title}")
        }
    }
}