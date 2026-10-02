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

// Sessionize's default format and level values - present on nearly every session, so they
// carry little information in a compact list row.
private const val DefaultSessionFormat = "Session"
private const val LightningTalkFormat = "Lightning talk"
private val SessionLevels = setOf("Introductory and overview", "Intermediate", "Advanced")

/**
 * The tags worth showing in a compact session list row, most useful first: the filtered-to
 * [selectedTracks], then the session's other tracks (e.g. a droidCon talk that's also swiftCon),
 * then other tags such as Keynote/Workshop, with levels last. Sessionize exports list tags as
 * format, level, then tracks (with repeats), so taking the first few raw tags would show
 * "Session, Intermediate" and hide the track. The details screen still shows every tag.
 */
fun SessionDetails.listTags(
    trackNames: Collection<String> = emptyList(),
    selectedTracks: Set<String> = emptySet(),
    maxTags: Int = 3,
): List<String> {
    val lightning = isLightning()
    return tags.distinct()
        .filterNot { it == DefaultSessionFormat || (lightning && it == LightningTalkFormat) }
        .sortedBy {
            when {
                it in selectedTracks -> 0
                it in trackNames -> 1
                it in SessionLevels -> 3
                else -> 2
            }
        }
        .take(maxTags)
}

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
