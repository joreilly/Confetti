package dev.johnoreilly.kikiconf.model

import dev.johnoreilly.kikiconf.GetSessionsQuery

data class Session(val id: String, val title: String, val description: String)

fun GetSessionsQuery.Session.mapToModel() = Session(id, title, description)