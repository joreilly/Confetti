package fr.androidmakers.server

import fr.androidmakers.server.model.*

interface DataSource {
    fun rooms(): List<Room>
    fun venue(id: String): Venue
    fun allSessions(): List<Session>
    fun sessions(first: Int, after: String?): SessionConnection
    fun speakers(): List<Speaker>
    fun partners(): List<PartnerGroup>
}

internal fun sliceSessions(sessionList: List<Session>, first: Int, after: String?): SessionConnection {
    val fromIndex = if (after == null) 0 else sessionList.indexOfFirst { it.id == after.decodeBase64() } + 1
    val toIndex = (fromIndex + first).coerceAtMost(sessionList.size)
    val sessionSubList = sessionList.subList(fromIndex = fromIndex, toIndex = toIndex)
    val edges = sessionSubList.map { session -> SessionEdge(node = session, cursor = session.id.encodeBase64()) }
    return SessionConnection(
        totalCount = sessionList.size,
        edges = edges,
        pageInfo = PageInfo(
            hasPreviousPage = fromIndex > 0,
            hasNextPage = toIndex < sessionList.size,
            startCursor = edges.first().cursor,
            endCursor = edges.last().cursor,
        )
    )
}

const val DATA_SOURCE_CONTEXT_KEY = "DATA_SOURCE_CONTEXT_KEY"