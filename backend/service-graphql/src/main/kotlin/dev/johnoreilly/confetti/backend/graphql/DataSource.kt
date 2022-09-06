package dev.johnoreilly.confetti.backend.graphql

interface DataSource {
  fun rooms(): List<Room>
  fun sessions(first: Int, after: String?): SessionConnection
  fun speakers(): List<Speaker>
  fun venue(id: String): Venue
  fun partnerGroups(): List<PartnerGroup>
  fun configuration(): Configuration
}