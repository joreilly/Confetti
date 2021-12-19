import dev.johnoreilly.kikiconf.KikiConfRepository

fun main() {
    val repo = KikiConfRepository()

    println("Sessions")
    val sessions = repo.sessions.value
    sessions.forEach { session ->
        println(session.title)
    }

    println("")
    println("Speakers")
    val speakers = repo.speakers.value
    speakers.forEach { speaker ->
        println(speaker.name)
    }

    println("")
    println("Rooms")
    val rooms = repo.rooms.value
    rooms.forEach { room ->
        println(room.name)
    }
}