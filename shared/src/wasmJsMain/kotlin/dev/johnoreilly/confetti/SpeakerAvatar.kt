package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionSpeakerDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails

actual fun SpeakerDetails.avatarUrl(): String? = photoUrlThumbnail
actual fun SessionSpeakerDetails.avatarUrl(): String? = photoUrlThumbnail
