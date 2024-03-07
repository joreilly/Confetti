package dev.johnoreilly.confetti.backend.datastore

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class DSession(
  val id: String,
  val type: String,
  val title: String,
  val description: String?,
  val shortDescription: String?,
  val language: String?,
  val start: LocalDateTime,
  val end: LocalDateTime,
  val complexity: String?,
  val feedbackId: String?,
  val tags: List<String>,
  val rooms: List<String>,
  val speakers: List<String>,
  val links: List<DLink>
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
  val floorPlanUrl: String?,
)

data class DSpeaker(
  val id: String,
  val name: String,
  val bio: String?,
  val tagline: String?,
  val company: String?,
  val companyLogoUrl: String?,
  val city: String?,
  val links: List<DLink>,
  val photoUrl: String?,
  val sessions: List<String>? = null
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
  val id: String,
  val name: String,
  val timeZone: String,
  val days: List<LocalDate> = emptyList(),
  val themeColor: String? = null
)

/**
 * @property nextPageCursor the cursor to the next page or null if there is no next page
 */
class DPage<T>(
  val items: List<T>,
  val nextPageCursor: String?,
)

class DOrderBy(
  val field: String,
  val direction: DDirection
)

sealed interface DComparator

object DComparatorEq: DComparator
object DComparatorGe: DComparator
object DComparatorLe: DComparator

class DFilter(
  val field: String,
  val comparator: DComparator,
  val value: Any,
)

enum class DDirection {
  ASCENDING,
  DESCENDING
}