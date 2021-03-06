import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.ConfettiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.io.File

suspend fun main() {
    // to workaround https://github.com/apollographql/apollo-kotlin/issues/3758 for now
    val file = File("confetti.db")
    val result = file.delete()

    val koin = initKoin().koin
    val repo = koin.get<ConfettiRepository>()

    println("Sessions")
    repo.sessions.collect { sessions ->
        sessions.forEach { session ->
            val startTimeString = repo.getSessionTime(session)
            println("${session.startInstant} $startTimeString ${session.title}")
        }
    }
}