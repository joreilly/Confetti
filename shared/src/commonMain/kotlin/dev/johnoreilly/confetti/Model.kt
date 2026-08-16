@file:OptIn(ExperimentalTime::class)

package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SessionSpeakerDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

fun SessionDetails.isBreak() = this.type == "break"
fun SessionDetails.isService() = this.type == "service"

fun SessionDetails.isLightning() = !isService() &&
    endsAt.toInstant(TimeZone.UTC)
    .minus(startsAt.toInstant(TimeZone.UTC))
    .inWholeMinutes <= 15

fun SessionDetails.sessionSpeakerLocation(): String {
    var text = if (speakers.isNotEmpty())
        speakers.joinToString(", ") { it.sessionSpeakerDetails.name }
    else
        ""
    text += " (${room?.name})"
    return text
}

fun SessionDetails.sessionSpeakers(): String? {
    return if (speakers.isNotEmpty()) {
        speakers.joinToString(", ") { it.sessionSpeakerDetails.name }
    } else {
        null
    }
}


fun SpeakerDetails.fullNameAndCompany(): String {
    return name + if (company.isNullOrBlank()) "" else ", " + this.company
}

fun SessionSpeakerDetails.fullNameAndCompany(): String {
    return name + if (company.isNullOrBlank()) "" else ", " + this.company
}


data class Conference(val id: String, val name: String)
