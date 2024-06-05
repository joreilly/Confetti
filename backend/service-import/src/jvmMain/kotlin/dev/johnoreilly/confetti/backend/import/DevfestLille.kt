import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DConfig
import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DVenue
import dev.johnoreilly.confetti.backend.import.Sessionize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync


@Serializable
data class DFAgenda(
    val schedules: List<DFSchedule>,
    val sessions: List<DFSession>,
    val speakers: List<DFSpeaker>
)

@Serializable
data class DFSchedule(
    val session_id: String?,
    val room: String,
    val start_time: String,
    val end_time: String,
)

@Serializable
data class DFSession(
    val id: String,
    val title: String,
    val abstract: String? = null,
    val speakers: List<String>? = null,
    val language: String? = null
)

@Serializable
data class DFSpeaker(
    val id: String,
    val display_name: String,
    val bio: String,
    val company: String?,
    val photo_url: String?,
)

val url = "https://cms4partners-ce427.nw.r.appspot.com/events/devfest-lille-2024/agenda"


object DevfestLille {
    suspend fun import() = Sessionize.writeData(
        sessionizeData = getAgenda().toData(),
        config = DConfig(
            id = ConferenceId.DevFestLille2024.id,
            name = "Devfest Lille 2024",
            timeZone = "Europe/Paris",
            themeColor = "0xFF7A54F6"
        ),
        venue = DVenue(
            id = "main",
            name = "Lille Grand Palais",
            address = "1 Bd des Cités Unies, 59777 Lille",
            description = mapOf(
                "en" to "Lille Grand Palais",
            ),
            latitude = 50.6326569,
            longitude = 3.0755021,
            imageUrl = "https://upload.wikimedia.org/wikipedia/fr/7/7a/Lille_grand_palais_entree.JPG",
            floorPlanUrl = null
        )
    )
}

private val json = Json {
    ignoreUnknownKeys = true
}

internal suspend fun getAgenda(): DFAgenda {
    return json.decodeFromString(getUrl2(url))
}

private suspend fun getUrl2(url: String): String {
    val request = Request(url.toHttpUrl()).newBuilder()
        .addHeader("accept", "application/json; version=4")
        .build()

    val response = OkHttpClient().newCall(request).executeAsync()

    return response.use {
        check(it.isSuccessful) {
            "Cannot get $url: ${it.body.string()}"
        }

        withContext(Dispatchers.IO) {
            response.body.string()
        }
    }
}

internal fun DFAgenda.toData(): Sessionize.SessionizeData {
    val rooms = schedules.map { it.room }.toSet().map {
        DRoom(
            it,
            it
        )
    }

    val speakers = speakers.map { dfSpeaker ->
        DSpeaker(
            id = dfSpeaker.id,
            name = dfSpeaker.display_name,
            bio = dfSpeaker.bio,
            null,
            company = dfSpeaker.company,
            companyLogoUrl = null,
            city = null,
            links = emptyList(),
            photoUrl = dfSpeaker.photo_url,
            sessions = sessions.filter { it.speakers.orEmpty().contains(dfSpeaker.id) }.map { it.id }
        )
    }

    val sessions = schedules.mapNotNull { dfSlot ->
        val dfSession = sessions.firstOrNull { it.id  == dfSlot.session_id }
        if (dfSession == null || dfSession.speakers == null) {
            return@mapNotNull null
        }
        DSession(
            id = dfSession.id,
            title = dfSession.title,
            type = "talk",
            description = dfSession.abstract,
            shortDescription = null,
            tags = emptyList(),
            language = dfSession.language,
            start = LocalDateTime.parse(dfSlot.start_time),//.toLocalDateTime(TimeZone.of("Europe/Paris")),
            end = LocalDateTime.parse(dfSlot.end_time),//.toLocalDateTime(TimeZone.of("Europe/Paris")),
            feedbackId = null,
            links = emptyList(),
            complexity = null,
            rooms = listOf(dfSlot.room),
            speakers = dfSession.speakers
        )
    }

    return Sessionize.SessionizeData(
        rooms = rooms,
        speakers = speakers,
        sessions = sessions
    )
}