package dev.johnoreilly.confetti.wear

import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

object TestFixtures {
    val sessionTime = LocalDateTime(2023, 1, 4, 9, 30)
    val date = sessionTime.date

    val sessionDetails = SessionDetails(
        id = "14997",
        title = "Kotlin DevRoom Welcoming Remarks",
        type = "talk",
        startsAt = sessionTime,
        endsAt = sessionTime.toInstant(TimeZone.UTC).plus(1.hours)
            .toLocalDateTime(TimeZone.UTC),
        sessionDescription = "Welcoming participants to the Kotlin DevRoom @ FOSDEM 2023 - We're back in person!",
        language = "en-US",
        speakers = listOf(
            SessionDetails.Speaker(
                __typename = "Speaker",
                speakerDetails = SpeakerDetails(
                    id = "6477",
                    name = "Nicola Corti",
                    photoUrl = null,
                    company = null,
                    companyLogoUrl = null,
                    city = null,
                    bio = null,
                    socials = listOf()
                )
            ), SessionDetails.Speaker(
                __typename = "Speaker",
                speakerDetails = SpeakerDetails(
                    id = "7079",
                    name = "Martin Bonnin",
                    photoUrl = null,
                    company = null,
                    companyLogoUrl = null,
                    city = null,
                    bio = null,
                    socials = listOf()
                )
            ), SessionDetails.Speaker(
                __typename = "Speaker",
                speakerDetails = SpeakerDetails(
                    id = "7934",
                    name = "Holger Steinhauer",
                    photoUrl = null,
                    company = null,
                    companyLogoUrl = null,
                    city = null,
                    bio = null,
                    socials = listOf()
                )
            )
        ),
        room = SessionDetails.Room(name = "UB5.230"),
        tags = listOf("Kotlin"),
        __typename = ""
    )
}