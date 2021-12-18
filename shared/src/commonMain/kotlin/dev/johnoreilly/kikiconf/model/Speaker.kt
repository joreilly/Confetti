package dev.johnoreilly.kikiconf.model

import dev.johnoreilly.kikiconf.GetSpeakersQuery

data class Speaker(val id: String, val name: String, val company: String?, val photoUrl: String)

fun GetSpeakersQuery.Speaker.mapToModel() = Speaker(id, name, company, photoUrl)