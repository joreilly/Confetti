import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.ConfettiRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

fun main()  {
    val koin = initKoin().koin
    val repo = koin.get<ConfettiRepository>()

    runBlocking {
        println("Sessions")
        repo.sessions.collect { sessions ->
            sessions.forEach { session ->
                println(session.title)
            }
        }
    }
}