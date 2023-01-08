package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionDetails

// needed for iOS client as "description" is reserved
fun SessionDetails.sessionDescription() = this.description

fun SessionDetails.sessionSpeakerLocation(): String {
    var text = if (speakers.isNotEmpty())
        speakers.joinToString(", ") { it.name }
    else
        ""
    text += " (${room?.name})"
    return text
}

