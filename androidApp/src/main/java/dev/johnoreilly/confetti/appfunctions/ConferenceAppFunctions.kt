package dev.johnoreilly.confetti.appfunctions

import android.app.PendingIntent
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSchemaCapability
import androidx.appfunctions.AppFunctionSchemaDefinition
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.apollographql.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime


@AppFunctionSchemaCapability
interface OpenIntent {
     val intentToOpen: PendingIntent?
}

@AppFunctionSchemaDefinition(name = "conferenceInfo", version = 1, category = "Conference")
interface ConferenceInfoSchemaDefinition {
    suspend fun conferenceInfo(
        appFunctionContext: AppFunctionContext,
    ): AppFunctionConference?
}

@AppFunctionSchemaDefinition(name = "findSessions", version = 1, category = "Sessions")
interface ConferenceSessionsSchemaDefinition {
    suspend fun findSessions(
        appFunctionContext: AppFunctionContext,
        findSessionsParams: FindSessionsParams,
    ): List<AppFunctionSession>?
}

@AppFunctionSchemaDefinition(name = "listSpeakers", version = 1, category = "Speakers")
interface ConferenceSpeakersSchemaDefinition {
    suspend fun listSpeakers(
        appFunctionContext: AppFunctionContext,
    ): List<AppFunctionSpeaker>?
}

@AppFunctionSerializable
data class FindSessionsParams(
    val speaker: String? = null,
)

@AppFunctionSerializable
data class AppFunctionSession(
    val id: String,
    val title: String,
    val room: String,
    val speakers: List<String>,
    val time: LocalDateTime,
    override val intentToOpen: PendingIntent? = null
): OpenIntent

@AppFunctionSerializable
data class AppFunctionSpeaker(
    val id: String,
    val name: String,
    override val intentToOpen: PendingIntent? = null
): OpenIntent

@AppFunctionSerializable
data class AppFunctionConference(
    val id: String,
    val title: String,
    val dates: List<LocalDateTime>,
    override val intentToOpen: PendingIntent? = null
): OpenIntent

class ConferenceAppFunctions : KoinComponent, ConferenceInfoSchemaDefinition, ConferenceSessionsSchemaDefinition,
    ConferenceSpeakersSchemaDefinition {
    private val confettiRepository: ConfettiRepository by inject()

    @AppFunction
    override suspend fun conferenceInfo(appFunctionContext: AppFunctionContext): AppFunctionConference {
        try {
            val conference = "kotlinconf2025"//confettiRepository.getConference()
            println("conferenceInfo, conference = $conference")
            val details =
                confettiRepository.conferenceData(conference, fetchPolicy = FetchPolicy.CacheFirst).data!!

            return AppFunctionConference(
                details.config.id,
                details.config.name,
                details.config.days.map { it.toJavaLocalDate().atStartOfDay() })
        } catch (e: Exception) {
                e.printStackTrace()
            throw e
        }
    }

    @AppFunction
    override suspend fun findSessions(
        appFunctionContext: AppFunctionContext,
        findSessionsParams: FindSessionsParams
    ): List<AppFunctionSession> {
        val conference = confettiRepository.getConference()
        val sessions = confettiRepository.sessions(conference, null, null, FetchPolicy.CacheOnly).data?.sessions?.nodes?.map { it.sessionDetails }.orEmpty()

        return sessions.map { session ->
            AppFunctionSession(
                session.id,
                session.title,
                session.room?.name ?: "",
                session.speakers.map { it.speakerDetails.name },
                session.startsAt.toJavaLocalDateTime(),
            )
        }
    }

    @AppFunction
    override suspend fun listSpeakers(appFunctionContext: AppFunctionContext): List<AppFunctionSpeaker> {
        val conference = confettiRepository.getConference()
        val results =
            confettiRepository.conferenceData(conference, fetchPolicy = FetchPolicy.CacheOnly).data?.speakers?.nodes?.map { it.speakerDetails }.orEmpty()

        return results.map { speaker ->
            AppFunctionSpeaker(
                speaker.id,
                speaker.name
            )
        }
    }
}