import dev.johnoreilly.kikiconf.KikiConfRepository

suspend fun main() {
    val repo = KikiConfRepository()
    val sessions = repo.getSessions()
    sessions.forEach { session ->
        println(session.title)
    }
}