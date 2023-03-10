package dev.johnoreilly.confetti.backend.graphql

interface DataSource {
  fun rooms(): List<Room>
  fun sessions(first: Int, after: String?, filter: SessionFilter?, orderBy: SessionOrderBy?): SessionConnection
  fun speakers(): List<Speaker>
  fun venues(): List<Venue>
  fun partnerGroups(): List<PartnerGroup>
  fun conference(): Conference
}