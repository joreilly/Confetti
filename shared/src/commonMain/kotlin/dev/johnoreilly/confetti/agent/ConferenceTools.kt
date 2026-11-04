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

private suspend fun ConfettiRepository.allSpeakers(conference: String): List<SpeakerDetails> {
    return conferenceData(conference, FetchPolicy.CacheFirst)
        .data
        ?.speakers
        ?.nodes
        ?.map { it.speakerDetails }
        .orEmpty()
}

/** Caps on list-tool output, so results fit in a small (e.g. on-device) model's context window. */
data class ToolOutputLimits(
    val maxResults: Int = Int.MAX_VALUE,
    val maxTextChars: Int = Int.MAX_VALUE,
) {
    companion object {
        val Compact = ToolOutputLimits(maxResults = 5, maxTextChars = 200)
    }
}

private fun String.truncate(maxChars: Int): String =
    if (length <= maxChars) this else take(maxChars).trimEnd() + "…"

private fun SessionDetails.summary(maxTextChars: Int = Int.MAX_VALUE): String = buildString {
    append("Id: ").append(id).append('\n')
    append("Title: ").append(title).append('\n')
    append("Type: ").append(type).append('\n')
    append("Starts: ").append(startsAt).append('\n')
    append("Ends: ").append(endsAt).append('\n')
    room?.let { append("Room: ").append(it.name).append('\n') }
    if (speakers.isNotEmpty()) {
        append("Speakers: ")
            .append(speakers.joinToString { it.sessionSpeakerDetails.name })
            .append('\n')
    }
    sessionDescription?.let { append("Description: ").append(it.truncate(maxTextChars)).append('\n') }
}

private fun SpeakerDetails.summary(maxTextChars: Int = Int.MAX_VALUE): String = buildString {
    append("Id: ").append(id).append('\n')
    append("Name: ").append(name).append('\n')
    company?.let { append("Company: ").append(it).append('\n') }
    tagline?.let { append("Tagline: ").append(it).append('\n') }
    bio?.let { append("Bio: ").append(it.truncate(maxTextChars)).append('\n') }
    if (sessions.isNotEmpty()) {
        append("Sessions: ")
            .append(sessions.joinToString { "${it.title} (${it.id})" })
            .append('\n')
    }
}

class GetSessionsTool(
    private val repository: ConfettiRepository,
    private val conference: String,
    private val limits: ToolOutputLimits = ToolOutputLimits(),
) : SimpleTool<GetSessionsTool.Args>(
    argsType = typeToken<Args>(),
    name = "GetSessionsTool",
    description = "Returns sessions whose title or description contains the filter as a " +
        "whole-word literal match (case-insensitive). Use this only when the user gives " +
        "an exact phrase to match verbatim. For topic/theme queries use SearchSessionsTool " +
        "instead — that one understands meaning, not just literal text.",
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
            val pattern = Regex("\\b${Regex.escape(args.filter.trim())}\\b", RegexOption.IGNORE_CASE)
            sessions.filter { session ->
                pattern.containsMatchIn(session.title) ||
                    pattern.containsMatchIn(session.sessionDescription.orEmpty())
            }
        }
        val limit = args.limit.coerceIn(1, limits.maxResults)
        return filtered.take(limit).joinToString(separator = "\n\n") { it.summary(limits.maxTextChars) }
            .ifEmpty { "No matching sessions." }
    }
}

class SearchSessionsTool(
    private val index: SessionEmbeddingIndex,
    private val limits: ToolOutputLimits = ToolOutputLimits(),
) : SimpleTool<SearchSessionsTool.Args>(
    argsType = typeToken<Args>(),
    name = "SearchSessionsTool",
    description = "Semantic search over sessions. Returns sessions ranked by how close " +
        "their title and description are in meaning to the given query, along with a " +
        "similarity score in [-1, 1] (higher is more relevant). Use this for any topic " +
        "or theme query — including single-word topics like \"AI\", \"testing\", or " +
        "\"UI\" — since it surfaces related talks even when they use different words.",
) {
    @Serializable
    data class Args(
        @property:LLMDescription("Natural-language description of the topic the user is interested in.")
        val query: String,
        @property:LLMDescription("Maximum number of sessions to return. Default 20.")
        val topK: Int = 20,
    )

    override suspend fun execute(args: Args): String {
        val matches = index.search(args.query, args.topK.coerceIn(1, limits.maxResults))
        if (matches.isEmpty()) return "No matching sessions."
        return matches.joinToString(separator = "\n\n") { scored ->
            "Score: ${(scored.score * 1000).toInt() / 1000.0}\n${scored.session.summary(limits.maxTextChars)}"
        }
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
    private val limits: ToolOutputLimits = ToolOutputLimits(),
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
            val pattern = Regex("\\b${Regex.escape(args.filter.trim())}\\b", RegexOption.IGNORE_CASE)
            speakers.filter { speaker ->
                pattern.containsMatchIn(speaker.name) ||
                    pattern.containsMatchIn(speaker.company.orEmpty()) ||
                    pattern.containsMatchIn(speaker.tagline.orEmpty()) ||
                    pattern.containsMatchIn(speaker.bio.orEmpty())
            }
        }
        val limit = args.limit.coerceIn(1, limits.maxResults)
        return filtered.take(limit).joinToString(separator = "\n\n") { it.summary(limits.maxTextChars) }
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
