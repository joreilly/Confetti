package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionDetails

fun SessionDetails.sessionSpeakerInfo(): String {
    var text = if (speakers.isNotEmpty())
        speakers.joinToString(", ") { it.name }
    else
        ""
    text += " (${rooms.joinToString { it.name }}})"
    return text
}

