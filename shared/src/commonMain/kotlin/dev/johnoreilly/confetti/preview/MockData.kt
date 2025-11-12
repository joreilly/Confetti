package dev.johnoreilly.confetti.preview

import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.datetime.LocalDateTime


const val JohnUrl = "https://sessionize.com/image/48e7-400o400o2-HkquSQhsfczBGkrABwVTBc.jpg"
val JohnOreilly = SessionDetails.Speaker(
    __typename = "Speaker",
    id = "0392772c-28d4-47f6-bd39-47d743fb4a81",
    speakerDetails = SpeakerDetails(
        id = "0392772c-28d4-47f6-bd39-47d743fb4a81",
        name = "John O'Reilly",
        photoUrl = JohnUrl,
        photoUrlThumbnail = JohnUrl,
        tagline = "Software Engineer",
        company = null,
        companyLogoUrl = null,
        city = null,
        bio = "John is a Kotlin GDE that has been developing Android apps since 2010. He worked on server side Java applications in the 2000s and desktop clients in the 1990s. He's also been exploring and advocating for all things Kotlin Multiplatform since 2018.",
        socials = emptyList(),
        sessions = emptyList(),
        __typename = "Speaker",
    )
)

const val MartinUrl = "https://sessionize.com/image/7c96-400o400o2-UiWeCMZDxPejrFsozKmLYr.jpeg"
val MartinBonnin = SessionDetails.Speaker(
    __typename = "Speaker",
    id = "56fda597-4927-4d25-9a80-4795d15ef080",
    speakerDetails = SpeakerDetails(
        id = "56fda597-4927-4d2y65-9a80-4795d15ef080",
        name = "Martin Bonnin",
        photoUrl = MartinUrl,
        photoUrlThumbnail = MartinUrl,
        tagline = "Software Engineer",
        company = null,
        companyLogoUrl = null,
        city = null,
        bio = "Martin is a maintainer of Apollo Kotlin. He has been writing Android applications since Cupcake and fell in love with Kotlin in 2017. Martin loves naming things and the sound of his laptop fan compiling all these type-safe programs. When not busy rewriting all his bash scripts in Kotlin, Martin loves to hike the Pyrénées or play a good game of Hearthstone.",
        socials = emptyList(),
        sessions = emptyList(),
        __typename = "Speaker",
    )
)

val sessionDetails = SessionDetails(
    id = "368995",
    title = "Confetti: building a Kotlin Multiplatform conference app in 40min",
    type = "talk",
    startsAt = LocalDateTime.parse("2023-04-13T14:00"),
    endsAt = LocalDateTime.parse("2023-04-13T14:45"),
    sessionDescription = """In this talk Martin and John will live code the development of Confetti, a fullstack conference app using a graphql-kotlin backend and KMM based mobile clients

The talk will cover
- development of the graphql-kotlin backend
- use of Apollo library and it's Kotlin Multiplatform support allowing addition of GraphQL queries and related logic in code shared between iOS and Android clients.
- development of mobile clients that consume the shared KMM code (using Jetpack Compose on Android and SwiftUI on iOS)""",
    language = "en-US",
    speakers = listOf(
        JohnOreilly,
        MartinBonnin
    ),
    room = SessionDetails.Room(name = "Effectenbeurszaal", id = "0", __typename = "Room"),
    tags = listOf(),
    __typename = "Session",
    recordingUrl = null
)
