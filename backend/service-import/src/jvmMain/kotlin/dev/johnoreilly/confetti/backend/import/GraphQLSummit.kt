package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import net.mbonnin.bare.graphql.asList
import net.mbonnin.bare.graphql.asMap
import net.mbonnin.bare.graphql.asString
import net.mbonnin.bare.graphql.toAny
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import java.time.ZoneId

object GraphQLSummit {
    private val okHttpClient = OkHttpClient()

    private fun getUrl(url: String, body: String): String {
        return Request.Builder()
            .url(url)
            .header(
                "x-cvent-csrf",
                "4d7e148fa2023d86fcf2a5e64536ec540af5e676ce5fcd7c902f8848f04ec90b756d54fa3ae8b40f213f5f8a0802fe6c84c9928bba3e1c164a661e81a032bb70"
            )
            .header("sec-ch-ua", "\"Google Chrome\";v=\"105\", \"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"105\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"macOS\"")
            .header(
                "cookie",
                "cvt_cookielocale=%5B%7B%22eventId%22%3A%229cabb0aa-eb3c-4bfd-bf74-3dcc4c82c066%22%2C%22locale%22%3A%22en-US%22%7D%5D; engage-auth=8f04c60759e743aa41129b7e5d57fd6c; cvt_id=CA1.84fdd5c51dbb66d3398c731bc93cfcefb3e10f06e921cd4b00f47682a4a299af; cvt_cookieconsent=[%229cabb0aa-eb3c-4bfd-bf74-3dcc4c82c066%22]; CSNSearchDeviceId=d3c22eab-fe8a-4fc8-92ed-ab527e6e41c4; CSNSearchSessionId=8eaffe3a-ce36-4bca-a432-389756952221; _adssid=7aefa298-f879-376e-fcad-2b94a0531d19; CSNSearchRFPSessionId=1773c929-ca22-3422-eb39-26bfdcf4e870; s_cc=true; s_sq=%5B%5BB%5D%5D; _dd_s="
            )
            .post(object : RequestBody() {
                override fun contentType(): MediaType? {
                    return "application/json".toMediaType()
                }

                override fun writeTo(sink: BufferedSink) {
                    sink.writeUtf8(body)
                }

            })
            .build()
            .let {
                okHttpClient.newCall(it).execute().also {
                    check(it.isSuccessful) {
                        "Cannot get $url: ${it.body?.string()}"
                    }
                }
            }.body!!.string()
    }

    private fun getJsonUrl(url: String, body: String) = Json.parseToJsonElement(getUrl(url, body)).toAny()

    private val sessionsBody =
        "{\"operationName\":\"SEARCH_SESSIONS\",\"variables\":{\"criteria\":{\"pageCriteria\":{\"limit\":20},\"timeframes\":[{\"start\":\"2022-10-04T22:00:00.000Z\",\"end\":\"2022-10-06T22:00:00.000Z\"}],\"categories\":[],\"admissionItemId\":\"c928fe6c-0a45-460a-9da6-9fa4fa1d9149\",\"useDisplayInAttendeeHub\":true,\"locale\":\"en-US\",\"exhibitorTranslationsFlag\":false}},\"query\":\"fragment BaseSession on Session {\\n  id\\n  name\\n  code\\n  start\\n  end\\n  description\\n  enrolled\\n  included\\n  featured\\n \\n  openForRegistration\\n  category {\\n    id\\n    name\\n    code\\n    __typename\\n  }\\n  location {\\n    id\\n    name\\n    code\\n    __typename\\n  }\\n  __typename\\n}\\n\\nquery SEARCH_SESSIONS(\$criteria: SearchSessionCriteria) {\\n  searchSessions(criteria: \$criteria) {\\n    pagination {\\n      currentToken\\n      nextToken\\n      previousToken\\n      limit\\n      totalCount\\n      __typename\\n    }\\n    data {\\n      ...BaseSession\\n      admissionItems\\n      upgrade\\n      openForAttendeeHub\\n      recommended\\n      exhibitors {\\n        id\\n        name\\n        description\\n        profileLogoId\\n        profileLogoUrl\\n        hidden\\n        featured\\n        eventSponsor\\n        sponsorshipLevelId\\n        __typename\\n      }\\n      webcast {\\n        id\\n        sessionId\\n        eventId\\n        format\\n        playerType\\n        player {\\n          id\\n          videoId\\n          videoUrl\\n          password\\n          playerTypeProvider\\n          stream {\\n            id\\n            status\\n            __typename\\n          }\\n          duration\\n          simuliveOffset\\n          __typename\\n        }\\n        links {\\n          recording {\\n            href\\n            __typename\\n          }\\n          __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}"

    private val sessionsDetails =
        "{\"operationName\":\"SESSION_DETAIL\",\"variables\":{\"id\":\"24942c4f-547e-4f9e-9377-d931ae1b70d1\",\"qaLimit\":9999,\"locale\":\"en-US\",\"exhibitorTranslationsFlag\":false},\"query\":\"query SESSION_DETAIL(\$id: ID!, \$qaLimit: Int, \$locale: String, \$exhibitorTranslationsFlag: Boolean) {\\n  sessionDetail(\\n    id: \$id\\n    qaLimit: \$qaLimit\\n    locale: \$locale\\n    exhibitorTranslationsFlag: \$exhibitorTranslationsFlag\\n  ) {\\n    id\\n    name\\n    description\\n    start\\n    end\\n    enrolled\\n    participant\\n    featured\\n    virtual\\n    included\\n    status\\n    openForRegistration\\n    openForAttendeeHub\\n    category {\\n      name\\n      __typename\\n    }\\n    location {\\n      name\\n      __typename\\n    }\\n    speakers {\\n      id\\n      firstName\\n      lastName\\n      company\\n      email\\n      title\\n      biography\\n      profileImageUri\\n      socialLinks {\\n        facebookUrl\\n        twitterUrl\\n        linkedInUrl\\n        __typename\\n      }\\n      __typename\\n    }\\n    webcast {\\n      id\\n      eventId\\n      sessionId\\n      format\\n      title\\n      sourceId\\n      provider\\n      meetingId\\n      details\\n      links {\\n        attendee {\\n          href\\n          code\\n          __typename\\n        }\\n        host {\\n          href\\n          code\\n          __typename\\n        }\\n        join {\\n          href\\n          code\\n          __typename\\n        }\\n        recording {\\n          href\\n          code\\n          __typename\\n        }\\n        speaker {\\n          href\\n          code\\n          __typename\\n        }\\n        __typename\\n      }\\n      playerType\\n      solutionType\\n      type\\n      simulatedLive\\n      onDemandVideo\\n      player {\\n        id\\n        videoId\\n        videoUrl\\n        password\\n        playerTypeProvider\\n        stream {\\n          id\\n          status\\n          recording {\\n            id\\n            status\\n            __typename\\n          }\\n          __typename\\n        }\\n        duration\\n        simuliveOffset\\n        __typename\\n      }\\n      __typename\\n    }\\n    settings {\\n      eventId\\n      sessionId\\n      qaEnabled\\n      qaCustomTimeFrameEnabled\\n      qaStartDateTime\\n      qaEndDateTime\\n      autoModerationEnabled\\n      __typename\\n    }\\n    questions {\\n      eventId\\n      sessionId\\n      items {\\n        id\\n        attendeeId\\n        attendeeName\\n        firstName\\n        lastName\\n        isAnonymous\\n        text\\n        answer\\n        status\\n        voteCount\\n        createDateTime\\n        statusChangeDateTime\\n        displayDateTime\\n        isCurrentQuestion\\n        isVoted\\n        __typename\\n      }\\n      __typename\\n    }\\n    customFields {\\n      id\\n      name\\n      value\\n      type\\n      __typename\\n    }\\n    documents {\\n      id\\n      name\\n      uri\\n      size\\n      extension\\n      __typename\\n    }\\n    chatSettings {\\n      isEnabled\\n      timeRange {\\n        startDate\\n        endDate\\n        __typename\\n      }\\n      stream {\\n        isAvailable\\n        channelId\\n        channelType\\n        __typename\\n      }\\n      __typename\\n    }\\n    feedbackSurvey {\\n      surveyUrl\\n      startOffset\\n      closeDate\\n      audience\\n      __typename\\n    }\\n    exhibitors {\\n      id\\n      name\\n      description\\n      profileLogoId\\n      profileLogoUrl\\n      hidden\\n      featured\\n      eventSponsor\\n      sponsorshipLevelId\\n      sponsorshipLevel {\\n        rank\\n        id\\n        name\\n        __typename\\n      }\\n      __typename\\n    }\\n    polls {\\n      pollId\\n      eventId\\n      sessionId\\n      name\\n      timeSetting\\n      isModeratorEditable\\n      __typename\\n    }\\n    participationThresholdSettings {\\n      participationThreshold\\n      participationLiveOnly\\n      __typename\\n    }\\n    widgets {\\n      widgetId\\n      sessionId\\n      widgetTitle\\n      widgetIcon\\n      code\\n      updatedAt\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}"

    private fun String.toRoom(): String {
        return if (this.isBlank()) {
            "all"
        } else {
            this
        }
    }

    fun import() {
        val data = getJsonUrl("https://web.cvent.com/hub/graphqlv2", sessionsBody)

        val rooms = mutableListOf<DRoom>()
        val speakers = mutableListOf<DSpeaker>()

        val sessions = data.asMap
            .get("data")
            .asMap
            .get("searchSessions")
            .asMap
            .get("data")
            .asList
            .asList.map {
                it.asMap
            }.map {

                val details = getJsonUrl(
                    "https://web.cvent.com/hub/graphqlv2",
                    sessionsDetails.replace("24942c4f-547e-4f9e-9377-d931ae1b70d1", it.get("id").asString)
                ).asMap.get("data").asMap.get("sessionDetail").asMap

                val roomId = it.get("location").asMap.get("id").asString
                if (rooms.none { it.id == roomId }) {
                    rooms.add(
                        DRoom(roomId, it.get("location").asMap.get("name").asString)
                    )
                }

                val thisSpeakers = details.get("speakers").asList.map { it.asMap }
                    .map {
                        DSpeaker(
                            name = "${it.get("firstName")} ${it.get("lastName")}",
                            company = it.get("company")?.asString,
                            bio = it.get("biography")?.asString,
                            photoUrl = it.get("profileImageUri")?.asString,
                            links = emptyList(),
                            city = null,
                            companyLogoUrl = null,
                            id = it.get("id").asString
                        )
                    }

                thisSpeakers.forEach { dspeaker ->
                    if (speakers.none { it.id == dspeaker.id }) {
                        speakers.add(dspeaker)
                    }
                }

                DSession(
                    id = it.get("id").asString,
                    type = "talk",
                    title = it.get("name").asString,
                    description = it.get("description")?.asString,
                    language = "en-US",
                    start = it.get("start").asString.let { Instant.parse(it) }
                        .toLocalDateTime(TimeZone.of("America/Los_Angeles")),
                    end = it.get("end").asString.let { Instant.parse(it) }
                        .toLocalDateTime(TimeZone.of("America/Los_Angeles")),
                    complexity = null,
                    feedbackId = null,
                    tags = emptyList(),
                    rooms = listOf(roomId),
                    speakers = thisSpeakers.map { it.id }
                )
            }

        DataStore().write(
            conf = "graphqlsummit2022",
            sessions = sessions.sortedBy { it.start },
            rooms = rooms,
            speakers = speakers,
            partnerGroups = emptyList(),
            config = DConfig(
                name = "GraphQL Summit",
                timeZone = "America/Los_Angeles"
            ),
            venues = listOf(
                DVenue(
                    id = "main",
                    name = "Sheraton San Diego",
                    address = "1380 Harbor Island Dr, San Diego, CA 92101, United States",
                    description = mapOf(
                        "en" to "Cool venue",
                        "fr" to "Venue fraiche",
                    ),
                    latitude = 32.7241602,
                    longitude = -117.2017836,
                    imageUrl = "https://images.cvent.com/beaf1dadc454450491db967b09a87f12/pix/67e5acbb935349fdb54897c74ee1d91a!_!0f5b7f31e3b929969df9edd8f4f09481.png?f=webp",
                    floorPlanUrl = null,
                )
            )
        )
    }
}