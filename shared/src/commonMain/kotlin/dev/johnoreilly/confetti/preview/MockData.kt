package dev.johnoreilly.confetti.preview

import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.decompose.Venue
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.datetime.LocalDate
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
        socials = listOf(
            SpeakerDetails.Social(__typename = "Social", name = "Twitter", url = "https://twitter.com/joreilly", icon = null),
            SpeakerDetails.Social(__typename = "Social", name = "Github", url = "https://github.com/joreilly", icon = null),
        ),
        sessions = listOf(
            SpeakerDetails.Session(
                __typename = "Session",
                id = "368995",
                title = "Confetti: building a Kotlin Multiplatform conference app in 40min",
                startsAt = LocalDateTime.parse("2023-04-13T14:00"),
            ),
        ),
        __typename = "Speaker",
    )
)

const val MartinUrl = "https://sessionize.com/image/7c96-400o400o2-UiWeCMZDxPejrFsozKmLYr.jpeg"
val MartinBonnin = SessionDetails.Speaker(
    __typename = "Speaker",
    id = "56fda597-4927-4d25-9a80-4795d15ef080",
    speakerDetails = SpeakerDetails(
        id = "56fda597-4927-4d25-9a80-4795d15ef080",
        name = "Martin Bonnin",
        photoUrl = MartinUrl,
        photoUrlThumbnail = MartinUrl,
        tagline = "Software Engineer",
        company = null,
        companyLogoUrl = null,
        city = null,
        bio = "Martin is a maintainer of Apollo Kotlin. He has been writing Android applications since Cupcake and fell in love with Kotlin in 2017. Martin loves naming things and the sound of his laptop fan compiling all these type-safe programs. When not busy rewriting all his bash scripts in Kotlin, Martin loves to hike the Pyrénées or play a good game of Hearthstone.",
        socials = listOf(
            SpeakerDetails.Social(__typename = "Social", name = "Twitter", url = "https://twitter.com/martin_bonnin", icon = null),
        ),
        sessions = listOf(
            SpeakerDetails.Session(
                __typename = "Session",
                id = "368995",
                title = "Confetti: building a Kotlin Multiplatform conference app in 40min",
                startsAt = LocalDateTime.parse("2023-04-13T14:00"),
            ),
        ),
        __typename = "Speaker",
    )
)

val johnOreillySpeaker: SpeakerDetails = JohnOreilly.speakerDetails
val martinBonninSpeaker: SpeakerDetails = MartinBonnin.speakerDetails

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
    tags = listOf("Kotlin", "Multiplatform", "GraphQL"),
    __typename = "Session",
    recordingUrl = null
)

val lightningSession = SessionDetails(
    id = "368996",
    title = "Compose tips in 5 minutes",
    type = "lightning",
    startsAt = LocalDateTime.parse("2023-04-13T14:50"),
    endsAt = LocalDateTime.parse("2023-04-13T14:55"),
    sessionDescription = "A fast lightning talk packed with Compose tips.",
    language = "en-US",
    speakers = listOf(JohnOreilly),
    room = SessionDetails.Room(name = "Veilingzaal", id = "1", __typename = "Room"),
    tags = listOf("Compose"),
    __typename = "Session",
    recordingUrl = null,
)

val breakSession = SessionDetails(
    id = "368997",
    title = "Coffee Break",
    type = "break",
    startsAt = LocalDateTime.parse("2023-04-13T15:00"),
    endsAt = LocalDateTime.parse("2023-04-13T15:30"),
    sessionDescription = null,
    language = null,
    speakers = emptyList(),
    room = null,
    tags = emptyList(),
    __typename = "Session",
    recordingUrl = null,
)

val mainRoom = RoomDetails(__typename = "Room", id = "0", name = "Effectenbeurszaal", capacity = 200)
val sideRoom = RoomDetails(__typename = "Room", id = "1", name = "Veilingzaal", capacity = 80)

val sampleSpeakers: List<SpeakerDetails> = listOf(
    johnOreillySpeaker,
    martinBonninSpeaker,
)

val sessionsSuccessState: SessionsUiState.Success = SessionsUiState.Success(
    now = LocalDateTime.parse("2023-04-13T14:10"),
    conference = "kotlinconf2023",
    conferenceName = "KotlinConf 2023",
    venueLat = null,
    venueLon = null,
    confDates = listOf(LocalDate.parse("2023-04-13")),
    formattedConfDates = listOf("Thu 13 Apr"),
    sessionsByStartTimeList = listOf(
        mapOf(
            "14:00" to listOf(sessionDetails),
            "14:50" to listOf(lightningSession),
            "15:00" to listOf(breakSession),
        )
    ),
    speakers = sampleSpeakers,
    rooms = listOf(mainRoom, sideRoom),
    bookmarks = setOf(sessionDetails.id),
    isRefreshing = false,
    searchString = "",
    selectedSessionId = null,
    notificationsActive = false,
)

val sampleVenue = Venue(
    id = "kotlinconf-venue",
    name = "Beurs van Berlage",
    address = "Damrak 243, 1012 ZJ Amsterdam, Netherlands",
    description = "Built in the late 19th century by architect Hendrik Petrus Berlage, the historic Beurs hosts KotlinConf in the heart of Amsterdam.",
    latitude = 52.375,
    longitude = 4.898,
    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/3/35/Beurs_van_Berlage_2008.jpg",
    floorPlanUrl = null,
    mapLink = "https://www.google.com/maps/search/?api=1&query=Beurs+van+Berlage",
)

val bookmarkedSessionsByDate: Map<LocalDateTime, List<SessionDetails>> = mapOf(
    LocalDateTime.parse("2023-04-13T14:00") to listOf(sessionDetails),
    LocalDateTime.parse("2023-04-13T14:50") to listOf(lightningSession),
)

val sampleConferences: List<GetConferencesQuery.Conference> = listOf(
    GetConferencesQuery.Conference(
        __typename = "Conference",
        id = "kotlinconf2023",
        timezone = "Europe/Amsterdam",
        days = listOf(LocalDate.parse("2023-04-13"), LocalDate.parse("2023-04-14")),
        name = "KotlinConf 2023",
        themeColor = "0x7F52FF",
    ),
    GetConferencesQuery.Conference(
        __typename = "Conference",
        id = "droidconlondon2023",
        timezone = "Europe/London",
        days = listOf(LocalDate.parse("2023-10-26"), LocalDate.parse("2023-10-27")),
        name = "droidcon London 2023",
        themeColor = "0xA4C639",
    ),
    GetConferencesQuery.Conference(
        __typename = "Conference",
        id = "kotlinconf2022",
        timezone = "Europe/Amsterdam",
        days = listOf(LocalDate.parse("2022-04-13"), LocalDate.parse("2022-04-14")),
        name = "KotlinConf 2022",
        themeColor = "0x7F52FF",
    ),
)

val conferencesByYear: Map<Int, List<GetConferencesQuery.Conference>> =
    sampleConferences.groupBy { it.days.firstOrNull()?.year ?: 2023 }

val previewConferenceListState: ConferencesComponent.Success =
    ConferencesComponent.Success(conferenceListByYear = conferencesByYear)
