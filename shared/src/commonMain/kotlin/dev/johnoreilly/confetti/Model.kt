package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails

// needed for iOS client as "description" is reserved
fun SessionDetails.sessionDescription() = this.description

fun SessionDetails.isBreak() = this.type == "break"

fun SessionDetails.sessionSpeakerLocation(): String {
    var text = if (speakers.isNotEmpty())
        speakers.joinToString(", ") { it.speakerDetails.name }
    else
        ""
    text += " (${room?.name})"
    return text
}


fun SpeakerDetails.fullNameAndCompany(): String {
    return name + if (company.isNullOrBlank()) "" else ", " + this.company
}


data class Conference(val id: String, val name: String)
