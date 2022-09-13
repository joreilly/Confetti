package dev.johnoreilly.confetti.backend.datastore

import kotlinx.datetime.LocalDateTime

data class DSession(
  val id: String,
  val type: String,
  val title: String,
  val description: String?,
  val language: String?,
  val start: LocalDateTime,
  val end: LocalDateTime,
  val complexity: String?,
  val feedbackId: String?,
  val tags: List<String>,
  val rooms: List<String>,
  val speakers: List<String>
)

class DRoom(
  val id: String,
  val name: String
)

class DVenue(
  val id: String,
  val name: String,
  val address: String?,
  val latitude: Double?,
  val longitude: Double?,
  val description: Map<String, String>,
  val imageUrl: String?,
)

class DSpeaker(
  val id: String,
  val name: String,
  val bio: String?,
  val company: String?,
  val companyLogoUrl: String?,
  val city: String?,
  val links: List<DLink>,
  val photoUrl: String?,
)

class DLink(
  val key: String,
  val url: String,
)

data class DPartnerGroup(
  val key: String,
  val partners: List<DPartner>,
)

data class DPartner(
  val name: String,
  val logoUrl: String,
  val url: String,
)

data class DConfig(
  val timeZone: String,
)

/**
 * @property nextPageCursor the cursor to the next page or null if there is no next page
 */
class DPage<T>(
  val items: List<T>,
  val nextPageCursor: String?,
)