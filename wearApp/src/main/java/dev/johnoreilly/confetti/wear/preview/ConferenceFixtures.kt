package dev.johnoreilly.confetti.wear.preview

import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.home.HomeUiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Hand-crafted fixtures for each conference we have a curated
 * [dev.johnoreilly.confetti.wear.ui.ConferenceTheme] for. The session
 * titles, room names, and dates are plausible stand-ins (not scraped from
 * real schedules) so the per-conference HomeScreen previews render
 * realistic content without pulling on the backend.
 */
object ConferenceFixtures {

    private fun session(
        id: String,
        title: String,
        roomName: String,
        startsAt: LocalDateTime,
    ): SessionDetails = TestFixtures.sessionDetails.copy(
        id = id,
        title = title,
        startsAt = startsAt,
        endsAt = startsAt,  // we only show the start on Home/Bookmarks
        room = SessionDetails.Room(name = roomName, id = roomName, __typename = "Room"),
    )

    // ------------------------------------------------------------------
    // KotlinConf — JetBrains purple · Code icon · Roboto Flex + Inter
    // ------------------------------------------------------------------

    val kotlinConf = GetConferencesQuery.Conference(
        __typename = "Conference",
        id = "kotlinconf2025",
        timezone = "Europe/London",
        days = listOf(
            LocalDate.parse("2025-05-21"),
            LocalDate.parse("2025-05-22"),
            LocalDate.parse("2025-05-23"),
        ),
        name = "KotlinConf 2025",
        themeColor = null,  // theme comes from ConferenceTheme lookup
    )
    val kotlinConfHome = HomeUiState(
        conference = kotlinConf.id,
        conferenceName = kotlinConf.name,
        confDates = kotlinConf.days,
    )
    val kotlinConfBookmarks = BookmarksUiState(
        conference = kotlinConf.id,
        upcoming = listOf(
            session("kc1", "The State of Kotlin Multiplatform", "Bella Sky 1", LocalDateTime.parse("2025-05-21T09:30")),
            session("kc2", "Coroutines Beyond the Basics", "Bella Sky 2", LocalDateTime.parse("2025-05-21T11:00")),
            session("kc3", "Compose on Every Surface", "Crown Hall", LocalDateTime.parse("2025-05-21T13:30")),
        ),
        past = emptyList(),
        now = LocalDateTime.parse("2025-05-21T09:00"),
    )

    // ------------------------------------------------------------------
    // AndroidMakers — Parisian ochre · Android icon
    // ------------------------------------------------------------------

    val androidMakers = GetConferencesQuery.Conference(
        __typename = "Conference",
        id = "androidmakers2025",
        timezone = "Europe/London",
        days = listOf(
            LocalDate.parse("2025-04-10"),
            LocalDate.parse("2025-04-11"),
        ),
        name = "Android Makers 2025",
        themeColor = null,
    )
    val androidMakersHome = HomeUiState(
        conference = androidMakers.id,
        conferenceName = androidMakers.name,
        confDates = androidMakers.days,
    )
    val androidMakersBookmarks = BookmarksUiState(
        conference = androidMakers.id,
        upcoming = listOf(
            session("am1", "Material 3 Expressive on Wear", "Beffroi Grande Salle", LocalDateTime.parse("2025-04-10T10:00")),
            session("am2", "Jetpack Glance Tiles", "Beffroi Petite Salle", LocalDateTime.parse("2025-04-10T11:30")),
            session("am3", "Adaptive Layouts for Foldables", "Beffroi Grande Salle", LocalDateTime.parse("2025-04-10T14:00")),
        ),
        past = emptyList(),
        now = LocalDateTime.parse("2025-04-10T09:30"),
    )

    // ------------------------------------------------------------------
    // Droidcon London — Droidcon green · Adb droid icon
    // ------------------------------------------------------------------

    val droidcon = GetConferencesQuery.Conference(
        __typename = "Conference",
        id = "droidconlondon2025",
        timezone = "Europe/London",
        days = listOf(
            LocalDate.parse("2025-10-23"),
            LocalDate.parse("2025-10-24"),
        ),
        name = "droidcon London",
        themeColor = null,
    )
    val droidconHome = HomeUiState(
        conference = droidcon.id,
        conferenceName = droidcon.name,
        confDates = droidcon.days,
    )
    val droidconBookmarks = BookmarksUiState(
        conference = droidcon.id,
        upcoming = listOf(
            session("dc1", "Gradle Build Speed in 2025", "Business Design Centre", LocalDateTime.parse("2025-10-23T09:45")),
            session("dc2", "KMP Shared UI, One Year In", "BDC Gallery", LocalDateTime.parse("2025-10-23T11:15")),
            session("dc3", "Room 3 and the New SQL", "BDC Mezzanine", LocalDateTime.parse("2025-10-23T13:45")),
        ),
        past = emptyList(),
        now = LocalDateTime.parse("2025-10-23T09:15"),
    )

    // ------------------------------------------------------------------
    // DevFest — Google Blue · Celebration icon · Google Sans Flex
    // ------------------------------------------------------------------

    val devFest = GetConferencesQuery.Conference(
        __typename = "Conference",
        id = "devfestnantes2025",
        timezone = "Europe/London",
        days = listOf(
            LocalDate.parse("2025-10-16"),
            LocalDate.parse("2025-10-17"),
        ),
        name = "DevFest Nantes",
        themeColor = null,
    )
    val devFestHome = HomeUiState(
        conference = devFest.id,
        conferenceName = devFest.name,
        confDates = devFest.days,
    )
    val devFestBookmarks = BookmarksUiState(
        conference = devFest.id,
        upcoming = listOf(
            session("df1", "Gemini in Flutter Apps", "La Cité Grand Auditorium", LocalDateTime.parse("2025-10-16T10:15")),
            session("df2", "Firebase AI Logic Tour", "Auditorium 450", LocalDateTime.parse("2025-10-16T11:45")),
            session("df3", "Android XR: First Principles", "Auditorium 800", LocalDateTime.parse("2025-10-16T14:15")),
        ),
        past = emptyList(),
        now = LocalDateTime.parse("2025-10-16T09:45"),
    )
}
