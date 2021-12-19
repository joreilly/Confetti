package dev.johnoreilly.kikiconf.model

import dev.johnoreilly.kikiconf.GetSessionQuery
import dev.johnoreilly.kikiconf.GetSessionsQuery

data class Session(val id: String, val title: String, val description: String, val tags: List<String>)

// should we move to useing graphql fragments to make this cleaner?
fun GetSessionQuery.Session.mapToModel() = Session(id, title, description, tags)
fun GetSessionsQuery.Session.mapToModel() = Session(id, title, description, tags)