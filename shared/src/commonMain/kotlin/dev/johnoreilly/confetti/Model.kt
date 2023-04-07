package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

fun SessionDetails.isBreak() = this.type == "break"

fun SessionDetails.isLightning() = endsAt.toInstant(TimeZone.UTC)
    .minus(startsAt.toInstant(TimeZone.UTC))
    .inWholeMinutes <= 15

fun SessionDetails.sessionSpeakerLocation(): String {
    var text = if (speakers.isNotEmpty())
        speakers.joinToString(", ") { it.speakerDetails.name }
    else
        ""
    text += " (${room?.name})"
    return text
}

fun SessionDetails.sessionSpeakers(): String? {
    return if (speakers.isNotEmpty()) {
        speakers.joinToString(", ") { it.speakerDetails.name }
    } else {
        null
    }
}


fun SpeakerDetails.fullNameAndCompany(): String {
    return name + if (company.isNullOrBlank()) "" else ", " + this.company
}


data class Conference(val id: String, val name: String)
