package dev.johnoreilly.confetti.backend.graphql

interface DataSource {
  fun rooms(): List<Room>
  fun sessions(
    first: Int,
    after: String?,
    filter: SessionFilter?,
    orderBy: SessionOrderBy?
  ): SessionConnection

  fun sessions(ids: List<String>): List<Session>
  fun speakers(first: Int, after: String?): SpeakerConnection
  fun speakers(ids: List<String>): List<Speaker>
  fun venues(): List<Venue>
  fun partnerGroups(): List<PartnerGroup>
  fun conference(): Conference
  fun bookmarks(): Set<String>

  fun addBookmark(sessionId: String): Set<String>
  fun removeBookmark(sessionId: String): Set<String>
  fun speaker(id: String): Speaker
  fun deleteUserData()
}
