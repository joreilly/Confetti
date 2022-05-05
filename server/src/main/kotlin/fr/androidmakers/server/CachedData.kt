package fr.androidmakers.server

import fr.androidmakers.server.model.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
object CachedData {
  private val json = Json {
    ignoreUnknownKeys = true
  }

  private const val MAX_AGE = 5 * 60 * 100
  private val client = OkHttpClient()

  private class Resource<T : Any>(val millis: Long, val data: T)

  private val resourceCache = mutableMapOf<String, Resource<*>>()
  private fun <T : Any> getResource(resourceName: String, block: (InputStream) -> T): T {
    val resource = resourceCache.get(resourceName)

    if (resource == null || System.currentTimeMillis() - resource.millis > MAX_AGE) {
      val url = "https://raw.githubusercontent.com/paug/android-makers-2022/main/data/database/$resourceName"
      val data = client.newCall(Request.Builder().url(url).build())
          .execute()
          .body!!
          .byteStream()
          .use {
            block(it)
          }
      resourceCache.put(resourceName, Resource(System.currentTimeMillis(), data))
    }

    return resourceCache.get(resourceName)?.data as? T ?: error("Error getting $resourceName")
  }

  fun rooms(): List<Room> {
      return getResource("rooms.json") {
        json.decodeFromStream<JsonRoomData>(it)
            .map {
              Room(
                  id = it.key,
                  capacity = it.value.capacity,
                  name = it.value.name,
                  level = it.value.level,
              )
            }
            .plus(
                // A failsafe "all" room
                Room(
                    id = "all",
                    capacity = Int.MAX_VALUE,
                    name = "Service",
                    level = "1"
                )
            )
      }
    }

  fun venue(id: String): Venue {
    return getResource("venues.json") {
      json.decodeFromStream<JsonVenueData>(it)
          .get(id)!!
          .let {
            Venue(
                name = it.name,
                address = it.address,
                description = it.description,
                descriptionFr = it.descriptionFr,
                coordinates = it.coordinates,
                imageUrl = it.imageUrl
            )
          }
    }
  }

  fun sessions(): List<Session> {
      val slots = getResource("schedule.json") {
        json.decodeFromStream<JsonSchedule>(it).toSlots()
      }
      return getResource("sessions.json") {
        json.decodeFromStream<JsonSessionData>(it)
            .mapNotNull { entry ->
              val slot = slots.firstOrNull {
                it.sessionId == entry.key
              }

              if (slot == null) {
                println("No slot found for session ${entry.key}")
                return@mapNotNull null
              }

              Session(
                  id = entry.key,
                  title = entry.value.title,
                  complexity = entry.value.complexity,
                  description = entry.value.description,
                  feedback = entry.value.feedback,
                  icon = entry.value.icon,
                  language = entry.value.language,
                  platformUrl = entry.value.platformUrl,
                  slido = entry.value.slido,
                  speakerIds = entry.value.speakers.toSet(),
                  tags = entry.value.tags,
                  startDate = slot.startDate,
                  endDate = slot.endDate,
                  roomId = slot.roomId
              )
            }
      }
    }

  fun speakers(): List<Speaker>   {
    return getResource("speakers.json") {
        json.decodeFromStream<JsonSpeakerData>(it)
            .map {
              Speaker(
                  id = it.key,
                  name = it.value.name,
                  bio = it.value.bio,
                  photoUrl = it.value.photoUrl,
                  company = it.value.company,
                  companyLogo = it.value.companyLogo,
                  country = it.value.country,
                  featured = it.value.featured,
                  order = it.value.order,
                  socials = it.value.socials.map { jsonSocial ->
                    Social(
                        icon = jsonSocial.icon,
                        name = jsonSocial.name,
                        link = jsonSocial.link
                    )
                  }
              )
            }
      }
    }

  fun partners(): List<PartnerGroup> {
    return getResource("partners.json") {
      json.decodeFromStream<JsonPartnerData>(it).map {
        PartnerGroup(
            it.order,
            it.title,
            it.items.map {
              Partner(
                  it.order,
                  it.name,
                  it.logoUrl,
                  it.url
              )
            }
        )
      }
    }
  }
}

private class Slot(
    val endDate: String,
    val sessionId: String,
    val roomId: String,
    val startDate: String
)

private fun JsonSchedule.toSlots(): List<Slot> {
  val list = mutableListOf<Slot>()
  for (result in this) {
    val day = result.value ?: return emptyList()
    val timeSlots = day.timeslots

    timeSlots.forEachIndexed { timeSlotIndex, timeSlot ->
      val sessions = timeSlot.sessions
      sessions.forEachIndexed { index, session ->
        val sessionId = session.items.firstOrNull()
        if (sessionId != null) {
          val startTime = timeSlot.startTime

          val extend = (session.extend)?.minus(1) ?: 0
          val endTime = timeSlots[timeSlotIndex + extend].endTime
          val roomId = when (sessions.size) {
            1 -> "all"
            else -> index.toString()
          }

          list.add(
              Slot(
                  startDate = getDate(result.key, startTime),
                  endDate = getDate(result.key, endTime),
                  roomId = roomId,
                  sessionId = sessionId
              )
          )
        }
      }
    }
  }

  return list.filter {
    it.sessionId != "no-op"
  }
}

/**
 * @param date the date as YYYY-MM-DD
 * @param time the time as HH:mm
 * @return a ISO86-01 String
 */
private fun getDate(date: String, time: String): String {
  val localDate = LocalDateTime.parse("${date}T$time")
  return localDate.atZone(ZoneId.of("Europe/Paris"))
      .toOffsetDateTime()
      .toString()
}