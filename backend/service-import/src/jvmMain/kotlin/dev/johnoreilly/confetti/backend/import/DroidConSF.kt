package dev.johnoreilly.confetti.backend.import


import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import okio.buffer
import okio.source
import xoxo.firstNonBlankTextContent
import xoxo.toXmlDocument
import xoxo.walkElements


private class SessionizeItem(
  val title: String,
  val room: String,
  val start: String,
  val end: String,
  val language: String,
  val speakers: List<SessionizeSpeaker>,
)

private class SessionizeSpeaker(
  val id: String,
  val name: String,
)

object DroidConSF {
  private fun items(): List<SessionizeItem> {
    return javaClass.classLoader!!.getResourceAsStream("sessionize.xml").source().buffer()
      .toXmlDocument()
      .root // <div class="sz-root">
      .walkElements()
      .filter {
        it.attributes.containsKey("data-sessionid")
      }
      .map {
        val room =
          it.walkElements().first { it.attributes["class"] == "sz-session__room" }
            .firstNonBlankTextContent()
        // "TimeWithDuration|en-US|2022-06-02T15:00:00.0000000Z|2022-06-02T16:30:00.0000000Z"
        val sztz = it.walkElements().mapNotNull { it.attributes["data-sztz"] }.first()
        val parts = sztz.split("|")
        val language = parts[1]
        val start = parts[2]
        val end = parts[3]
        val title =
          it.walkElements().first { it.attributes["class"] == "sz-session__title" }
            .firstNonBlankTextContent()
            .replace("\n", "")
            .replace(Regex("  *"), " ")

        val speakers =
          it.walkElements().filter { it.attributes.containsKey("data-speakerid") }.map {
            val name = it.firstNonBlankTextContent()
            SessionizeSpeaker(id = name, name = name)
          }.toList()

        SessionizeItem(
          title = title,
          room = room,
          language = language,
          start = start,
          end = end,
          speakers = speakers
        )
      }.toList()
  }

  fun import() {
    val items = items()

    val dataStore = DataStore()

    dataStore.write(
      conf = CONF,
      sessions = items.mapIndexed { index, item ->
        DSession(
          id = index.toString(),
          type = "talk",
          title = item.title,
          description = null,
          language = item.language,
          start = item.start.toInstant().toLocalDateTime(TimeZone.of(TIMEZONE)),
          end = item.end.toInstant().toLocalDateTime(TimeZone.of(TIMEZONE)),
          tags = emptyList(),
          rooms = listOf(item.room),
          speakers = item.speakers.map { it.id }
        )
      },
      rooms = items.map {
        DRoom(
          id = it.room,
          name = it.room
        )
      }.distinct(),
      speakers = items.flatMap {
        it.speakers.map {
          DSpeaker(
            id = it.id,
            name = it.name,
            bio = null,
            company = null,
            links = emptyList(),
            photoUrl = null
          )
        }
      },
      config = DConfig(
        timeZone = TIMEZONE
      )
    )
  }

  private const val CONF = "droidconsf"
  private const val TIMEZONE = "America/Los_Angeles"
}
