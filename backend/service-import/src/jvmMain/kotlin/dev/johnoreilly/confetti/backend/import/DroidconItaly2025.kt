package dev.johnoreilly.confetti.backend.import

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * CSV-based importer for droidcon Italy 2025 using
 * - droidcon-2025_speakers.csv
 * - droidcon-2025_sessions.csv
 */
object DroidconItaly2025 {
    private val csvReader = csvReader()
    private const val SPEAKERS_FILE = "droidcon-2025_speakers.csv"
    private const val SESSIONS_FILE = "droidcon-2025_sessions.csv"


    private val config = DConfig(
        id = ConferenceId.DroidconItaly2025.id,
        name = "droidcon Italy 2025",
        timeZone = "Europe/Rome",
        days = listOf(
            LocalDate(2025, 11, 19),
            LocalDate(2025, 11, 20)
        )
    )

    private val venue = DVenue(
        id = "main",
        name = "UCI Cinema Lingotto",
        address = "Via  Nizza 262, 10126 Turin",
        latitude = null,
        longitude = null,
        description = emptyMap(),
        imageUrl = "https://flutterheroes.com/2025/wp-content/uploads/sites/6/venue_pg3.jpg",
        floorPlanUrl = null
    )

    suspend fun import(): Int {
        val sessionsCsvText = javaClass.classLoader.getResourceAsStream(SESSIONS_FILE).use { it.reader().readText() }
        val speakersCsvText = javaClass.classLoader.getResourceAsStream(SPEAKERS_FILE).use { it.reader().readText() }

        val sessionsRows = csvReader.readAll(sessionsCsvText).drop(1)
        val speakersRows = csvReader.readAll(speakersCsvText).drop(1)

        data class Speaker(
            val id: String,
            val name: String,
            val bio: String?,
            val picture: String?,
            val company: String?,
            val jobTitle: String?,
            val xUrl: String?,
            val linkedinUrl: String?,
            val githubUrl: String?,
        )

        val speakers = speakersRows.mapNotNull { row ->
            // Expected columns: ID,Name,Biography,Picture,Proposal titles,Your company,Your job title,X (user URL),LinkedIn (user URL),Github (user URL)
            if (row.isEmpty()) return@mapNotNull null
            val id = row.getOrNull(0)?.trim().orEmpty()
            if (id.isBlank()) return@mapNotNull null
            Speaker(
                id = id,
                name = row.getOrNull(1)?.trim().orEmpty(),
                bio = row.getOrNull(2)?.trim().takeIf { !it.isNullOrBlank() },
                picture = row.getOrNull(3)?.trim().takeIf { !it.isNullOrBlank() },
                company = row.getOrNull(5)?.trim().takeIf { !it.isNullOrBlank() },
                jobTitle = row.getOrNull(6)?.trim().takeIf { !it.isNullOrBlank() },
                xUrl = row.getOrNull(7)?.trim().takeIf { !it.isNullOrBlank() },
                linkedinUrl = row.getOrNull(8)?.trim().takeIf { !it.isNullOrBlank() },
                githubUrl = row.getOrNull(9)?.trim().takeIf { !it.isNullOrBlank() },
            )
        }

        // Map by name for resolving speakers in sessions file
        val speakerByName: Map<String, Speaker> = speakers.associateBy { it.name }

        data class Session(
            val id: String,
            val title: String,
            val tags: List<String>,
            val abstractText: String?,
            val speakerNames: List<String>,
            val level: String?,
            val date: String?,
            val startTime: String?,
            val duration: String?,
            val room: String?,
        )

        val sessions = sessionsRows.mapNotNull { row ->
            // Expected columns: ID,Proposal title,Tags,Abstract,Speaker names,Level,Date,Start Time,Duration,Room
            if (row.isEmpty()) return@mapNotNull null
            val id = row.getOrNull(0)?.trim().orEmpty()
            if (id.isBlank()) return@mapNotNull null
            val title = row.getOrNull(1)?.trim().orEmpty()
            val tags = row.getOrNull(2)?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
            val abstractText = row.getOrNull(3)?.trim().takeIf { !it.isNullOrBlank() }
            val speakerNames = row.getOrNull(4)?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
            val level = row.getOrNull(5)?.trim().takeIf { !it.isNullOrBlank() }
            val date = row.getOrNull(6)?.trim().takeIf { !it.isNullOrBlank() }
            val startTime = row.getOrNull(7)?.trim().takeIf { !it.isNullOrBlank() }
            val duration = row.getOrNull(8)?.trim().takeIf { !it.isNullOrBlank() }
            val room = row.getOrNull(9)?.trim().takeIf { !it.isNullOrBlank() }
            Session(id, title, tags, abstractText, speakerNames, level, date, startTime, duration, room)
        }

        // Resolve speakers for sessions
        val datastoreSessions = sessions.map { s ->
            val speakerIds = s.speakerNames.mapNotNull { speakerByName[it]?.id }
            
            // Parse start and end times from CSV data
            val start = if (s.date != null && s.startTime != null) {
                LocalDateTime.parse("${s.date}T${s.startTime}")
            } else {
                defaultStart()
            }
            
            val end = if (s.date != null && s.startTime != null && s.duration != null) {
                // Duration is in format HH:MM, parse and add to start
                val durationParts = s.duration.split(":")
                val durationHours = durationParts.getOrNull(0)?.toIntOrNull() ?: 0
                val durationMinutes = durationParts.getOrNull(1)?.toIntOrNull() ?: 0
                
                // Add duration to start time
                var endHour = start.hour + durationHours
                var endMinute = start.minute + durationMinutes
                if (endMinute >= 60) {
                    endHour += 1
                    endMinute -= 60
                }
                
                LocalDateTime(start.year, start.monthNumber, start.dayOfMonth, endHour, endMinute)
            } else {
                defaultEnd()
            }
            
            val rooms = if (s.room != null) listOf(s.room) else listOf("Main")
            
            DSession(
                id = s.id,
                type = "talk",
                title = s.title,
                description = s.abstractText,
                shortDescription = null,
                language = null,
                start = start,
                end = end,
                complexity = s.level,
                feedbackId = null,
                tags = s.tags,
                rooms = rooms,
                speakers = speakerIds,
                links = emptyList()
            )
        }

        // Build speakers with back-linked sessions
        val speakerSessionsMap: Map<String, List<String>> = datastoreSessions
            .flatMap { session -> session.speakers.map { it to session.id } }
            .groupBy({ it.first }, { it.second })

        val datastoreSpeakers = speakers.map { sp ->
            DSpeaker(
                id = sp.id,
                name = sp.name,
                bio = sp.bio,
                tagline = sp.jobTitle,
                company = sp.company,
                companyLogoUrl = null,
                city = null,
                links = listOfNotNull(
                    sp.xUrl?.let { DLink("twitter", it) },
                    sp.linkedinUrl?.let { DLink("linkedin", it) },
                    sp.githubUrl?.let { DLink("github", it) },
                ),
                photoUrl = sp.picture,
                sessions = speakerSessionsMap[sp.id]
            )
        }

        DataStore().write(
            sessions = datastoreSessions,
            rooms = listOf(
                DRoom("Sala 7", "Sala 7"),
                DRoom("Sala 8", "Sala 8")
            ),
            speakers = datastoreSpeakers,
            partnerGroups = emptyList(),
            config = config,
            venues = listOf(venue)
        )

        return sessions.size
    }

    private fun defaultStart(): LocalDateTime {
        // Placeholder: Day 1 09:00
        return LocalDateTime.parse("2025-11-19T09:00")
    }

    private fun defaultEnd(): LocalDateTime {
        // Placeholder: Day 1 10:00
        return LocalDateTime.parse("2025-11-19T10:00")
    }

    private fun readFileFlexible(vararg candidates: String): String {
        // Try multiple relative paths to be resilient to working directory
        for (c in candidates) {
            val p: Path = Paths.get(c)
            if (Files.exists(p)) {
                return Files.readString(p)
            }
        }
        // Also try from project root if launched from module dir
        val moduleRoot = Paths.get("backend", "service-import")
        for (c in candidates) {
            val p = moduleRoot.resolve(Paths.get(c).fileName)
            if (Files.exists(p)) {
                return Files.readString(p)
            }
        }
        error("CSV file not found. Tried: ${candidates.joinToString()}")
    }

}
