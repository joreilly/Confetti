@file:OptIn(ExperimentalTime::class)

package dev.johnoreilly.confetti.agent

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import com.apollographql.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

private suspend fun ConfettiRepository.allSessions(conference: String): List<SessionDetails> {
    return sessionsQuery(conference, FetchPolicy.CacheFirst)
        .execute()
        .data
        ?.sessions
        ?.nodes
        ?.map { it.sessionDetails }
        .orEmpty()
}

private suspend fun ConfettiRepository.allSpeakers(conference: String): List<SpeakerDetails> {
    return conferenceData(conference, FetchPolicy.CacheFirst)
        .data
        ?.speakers
        ?.nodes
        ?.map { it.speakerDetails }
        .orEmpty()
}

private fun SessionDetails.summary(): String = buildString {
    append("Id: ").append(id).append('\n')
    append("Title: ").append(title).append('\n')
    append("Type: ").append(type).append('\n')
    append("Starts: ").append(startsAt).append('\n')
    append("Ends: ").append(endsAt).append('\n')
    room?.let { append("Room: ").append(it.name).append('\n') }
    if (speakers.isNotEmpty()) {
        append("Speakers: ")
            .append(speakers.joinToString { it.speakerDetails.name })
            .append('\n')
    }
    sessionDescription?.let { append("Description: ").append(it).append('\n') }
}

private fun SpeakerDetails.summary(): String = buildString {
    append("Id: ").append(id).append('\n')
    append("Name: ").append(name).append('\n')
    company?.let { append("Company: ").append(it).append('\n') }
    tagline?.let { append("Tagline: ").append(it).append('\n') }
    bio?.let { append("Bio: ").append(it).append('\n') }
    if (sessions.isNotEmpty()) {
        append("Sessions: ")
            .append(sessions.joinToString { "${it.title} (${it.id})" })
            .append('\n')
    }
}

class GetSessionsTool(
    private val repository: ConfettiRepository,
    private val conference: String,
) : SimpleTool<GetSessionsTool.Args>(
    argsType = typeToken<Args>(),
    name = "GetSessionsTool",
    description = "Returns the list of sessions for the current conference. " +
        "Optionally filter by a free-text query that is matched against the title and description.",
) {
    @Serializable
    data class Args(
        @property:LLMDescription("Optional free-text filter applied to session title and description. Use an empty string for all sessions.")
        val filter: String = "",
        @property:LLMDescription("Maximum number of sessions to return. Default 20.")
        val limit: Int = 20,
    )

    override suspend fun execute(args: Args): String {
        val sessions = repository.allSessions(conference)
        val filtered = if (args.filter.isBlank()) {
            sessions
        } else {
            val needle = args.filter.trim()
            sessions.filter { session ->
                session.title.contains(needle, ignoreCase = true) ||
                    session.sessionDescription.orEmpty().contains(needle, ignoreCase = true)
            }
        }
        val limit = args.limit.coerceAtLeast(1)
        return filtered.take(limit).joinToString(separator = "\n\n") { it.summary() }
            .ifEmpty { "No matching sessions." }
    }
}

class GetSessionByIdTool(
    private val repository: ConfettiRepository,
    private val conference: String,
) : SimpleTool<GetSessionByIdTool.Args>(
    argsType = typeToken<Args>(),
    name = "GetSessionByIdTool",
    description = "Returns the full details of a single session given its id.",
) {
    @Serializable
    data class Args(
        @property:LLMDescription("The session id to look up.")
        val sessionId: String,
    )

    override suspend fun execute(args: Args): String {
        val session = repository.allSessions(conference).firstOrNull { it.id == args.sessionId }
            ?: return "No session with id ${args.sessionId}."
        return session.summary()
    }
}

class GetSpeakersTool(
    private val repository: ConfettiRepository,
    private val conference: String,
) : SimpleTool<GetSpeakersTool.Args>(
    argsType = typeToken<Args>(),
    name = "GetSpeakersTool",
    description = "Returns the list of speakers for the current conference. " +
        "Optionally filter by a free-text query that is matched against name, company, tagline and bio.",
) {
    @Serializable
    data class Args(
        @property:LLMDescription("Optional free-text filter applied to speaker name, company, tagline and bio. Use an empty string for all speakers.")
        val filter: String = "",
        @property:LLMDescription("Maximum number of speakers to return. Default 20.")
        val limit: Int = 20,
    )

    override suspend fun execute(args: Args): String {
        val speakers = repository.allSpeakers(conference)
        val filtered = if (args.filter.isBlank()) {
            speakers
        } else {
            val needle = args.filter.trim()
            speakers.filter { speaker ->
                speaker.name.contains(needle, ignoreCase = true) ||
                    speaker.company.orEmpty().contains(needle, ignoreCase = true) ||
                    speaker.tagline.orEmpty().contains(needle, ignoreCase = true) ||
                    speaker.bio.orEmpty().contains(needle, ignoreCase = true)
            }
        }
        val limit = args.limit.coerceAtLeast(1)
        return filtered.take(limit).joinToString(separator = "\n\n") { it.summary() }
            .ifEmpty { "No matching speakers." }
    }
}

class GetSpeakerByIdTool(
    private val repository: ConfettiRepository,
    private val conference: String,
) : SimpleTool<GetSpeakerByIdTool.Args>(
    argsType = typeToken<Args>(),
    name = "GetSpeakerByIdTool",
    description = "Returns the full details of a single speaker given their id.",
) {
    @Serializable
    data class Args(
        @property:LLMDescription("The speaker id to look up.")
        val speakerId: String,
    )

    override suspend fun execute(args: Args): String {
        val speaker = repository.allSpeakers(conference).firstOrNull { it.id == args.speakerId }
            ?: return "No speaker with id ${args.speakerId}."
        return speaker.summary()
    }
}
