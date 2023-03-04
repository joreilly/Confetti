package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionDetails

fun SessionDetails.sessionSpeakerInfo() =
    if (speakers.isNotEmpty())
        speakers.joinToString(", ") { it.name }
    else
        ""

