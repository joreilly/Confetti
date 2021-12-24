import dev.johnoreilly.kikiconf.KikiConfRepository
import dev.johnoreilly.kikiconf.di.initKoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

fun main()  {
    val koin = initKoin().koin
    val repo = koin.get<KikiConfRepository>()

    runBlocking {
        println("Sessions")
        repo.sessions.collect { sessions ->
            sessions.forEach { session ->
                println(session.title)
            }
        }
    }
}