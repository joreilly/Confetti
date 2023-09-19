package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DConfig
import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DVenue
import dev.johnoreilly.confetti.backend.datastore.DataStore
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object SwiftConnection {
    private fun String.toRoom(): String {
        return if (this.isBlank()) {
            "all"
        } else {
            this
        }
    }

    suspend fun import(): Int {
        val root = Json.parseToJsonElement(javaClass.classLoader.getResourceAsStream("swiftconnection2023.json").use { it.reader().readText() })

        val speakers = root.jsonArray.flatMap {
            it.jsonObject.get("Speaker Names")!!.jsonPrimitive.content.split(",")
        }.filter { it.isNotBlank() }
            .toSet()
            .toList()
            .withIndex()
            .map {
                DSpeaker(
                    id = it.index.toString(),
                    name = it.value,
                    photoUrl = null,
                    bio = null,
                    tagline = null,
                    city = null,
                    company = null,
                    companyLogoUrl = null,
                    links = emptyList()
                )
            }

        val sessions = root.jsonArray.withIndex().map {
            val o = it.value.jsonObject
            val c = o.get("Time")!!.jsonPrimitive.content.split(" ")

            val day = c[1].replace(",","").toInt()
            val start = LocalDateTime(
                year = 2023,
                monthNumber = 9,
                dayOfMonth = day,
                hour = c[3].split(":")[0].toInt(),
                minute = c[3].split(":")[1].toInt()
            )
            val end = LocalDateTime(
                year = 2023,
                monthNumber = 9,
                dayOfMonth = day,
                hour = c[5].split(":")[0].toInt(),
                minute = c[5].split(":")[1].toInt()
            )

            val s = o.get("Speaker Names")!!.jsonPrimitive.content.split(",")
            DSession(
                id = it.index.toString(),
                type = o.get("Kind")!!.jsonPrimitive.content,
                title = o.get("Name")!!.jsonPrimitive.content,
                description = null,
                shortDescription = null,
                language = "en-US",
                start = start,
                end = end,
                complexity = null,
                feedbackId = null,
                tags = emptyList(),
                rooms = listOf("main"),
                speakers = speakers.filter { it.name in s }.map { it.id },
                links = emptyList()
            )
        }

        val rooms = listOf(DRoom("main", "main"))

        DataStore().write(
            sessions = sessions.sortedBy { it.start },
            rooms = rooms,
            speakers = speakers,
            partnerGroups = emptyList(),
            config = DConfig(
                id = ConferenceId.SwiftConnection2023.id,
                name = "Swift Connection",
                timeZone = "Europe/Paris",
                days = listOf(LocalDate(2023, 9, 21), LocalDate(2023, 9, 21))
            ),
            venues = listOf(
                DVenue(
                    id = "main",
                    name = "Pan Piper",
                    address = "En face du père Lachaise",
                    description = mapOf(
                        "en" to "Cool venue",
                        "fr" to "Venue fraiche",
                    ),
                    latitude = null,
                    longitude = null,
                    imageUrl = null,
                    floorPlanUrl = null,
                )
            )
        )

        return sessions.size
    }
}