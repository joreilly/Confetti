package dev.johnoreilly.kikiconf.model

import dev.johnoreilly.kikiconf.GetRoomsQuery

data class Room(val id: String, val name: String)

fun GetRoomsQuery.Room.mapToModel() = Room(id, name)