import dev.johnoreilly.kikiconf.KikiConfRepository

suspend fun main() {
    val repo = KikiConfRepository()

    println("Sessions")
    val sessions = repo.getSessions()
    sessions.forEach { session ->
        println(session.title)
    }

    println("")
    println("Speakers")
    val speakers = repo.getSpeakers()
    speakers.forEach { speaker ->
        println(speaker.name)
    }

    println("")
    println("Rooms")
    val rooms = repo.getRooms()
    rooms.forEach { room ->
        println(room.name)
    }

}