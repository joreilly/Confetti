@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.complication

import android.app.PendingIntent.*
import android.content.Intent
import androidx.core.net.toUri
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.complication.DataComplicationService
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.toTimeZone
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.android.ext.android.inject

class NextSessionComplicationService :
    DataComplicationService<NextSessionComplicationData, NextSessionTemplate>() {
    override val renderer: NextSessionTemplate = NextSessionTemplate(this)

    private val repository: ConfettiRepository by inject()

    override suspend fun data(request: ComplicationRequest): NextSessionComplicationData {
        val conference = repository.getConference()
        val data = repository.conferenceData(conference, FetchPolicy.CacheOnly).data

        val nextSession =  if (data == null) {
            null
        } else {
            data.sessions.nodes.map { it.sessionDetails }
                .nextSessionOrNull(data.config.timezone.toTimeZone())
        }

        val launchIntent = if (nextSession != null) {
            val sessionDetailIntent = Intent(
                Intent.ACTION_VIEW,
                "confetti://confetti/session/".toUri()
            )

            getActivity(
                this,
                0,
                sessionDetailIntent,
                FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
            )
        } else {
            null
        }

        return NextSessionComplicationData(nextSession, launchIntent)
    }

    override fun previewData(type: ComplicationType): NextSessionComplicationData =
        renderer.previewData()
}

fun List<SessionDetails>.nextSessionOrNull(timeZone: TimeZone): SessionDetails?  {
        val today = Clock.System.todayIn(timeZone)
        val now = Clock.System.now().toLocalDateTime(timeZone)

        return filter {
            it.startsAt > now && it.startsAt.date == today
        }.minByOrNull {
            it.startsAt
        }
    }