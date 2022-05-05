package fr.androidmakers.server.model

import fr.androidmakers.server.CachedData
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class Room(
  val id: String,
  val name: String,
  val capacity: Int,
  val level: String
)

data class Session(
  val id: String,
  val title: String,
  val description: String,
  val language: String?,
  private val speakerIds: Set<String>,
  val complexity: String?,
  val tags: List<String>,
  val icon: String?,
  val platformUrl: String?,
  val feedback: String?,
  val slido: String?,
  @Deprecated("Use startInstant instead")
  val startDate: String,
  @Deprecated("Use endInstant instead")
  val endDate: String,
  private val roomId: String,
) {
  val startInstant: Instant = Instant.parse(startDate)
  val endInstant: Instant = Instant.parse(endDate)

  val speakers: List<Speaker>
    get() {
      return CachedData.speakers().filter {
        speakerIds.contains(it.id)
      }
    }
  // A session might not have a room yet
  val room: Room
    get() {
      return CachedData.rooms().single {
        it.id == roomId
      }
    }
}

data class Speaker(
  val id: String,
  val order: Float?,
  val featured: Boolean,
  val name: String,
  val bio: String,
  val country: String?,
  val companyLogo: String?,
  val company: String?,
  val socials: List<Social>,
  val photoUrl: String
)

data class Social(
  val icon: String,
  val link: String,
  val name: String
)

data class PartnerGroup(
    val order: Int,
    val title: String,
    val partners: List<Partner>
)

data class Partner(
    val order: Int,
    val name: String,
    val logoUrl: String,
    val url: String
)


data class Venue(
    val name: String,
    val address: String? = null,
    val coordinates: String? = null,
    val description: String,
    val descriptionFr: String,
    val imageUrl: String,
)

data class Configuration(
    val timezone: String
)