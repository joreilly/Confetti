@file:OptIn(ExperimentalHorologistTilesApi::class)

package dev.johnoreilly.confetti.wear.complication

import android.app.PendingIntent.*
import android.content.Intent
import androidx.core.net.toUri
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.google.android.horologist.tiles.ExperimentalHorologistTilesApi
import com.google.android.horologist.tiles.complication.DataComplicationService
import dev.johnoreilly.confetti.ConfettiRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.android.ext.android.inject

class NextSessionComplicationService :
    DataComplicationService<NextSessionComplicationData, NextSessionTemplate>() {
    override val renderer: NextSessionTemplate = NextSessionTemplate(this)

    private val repository: ConfettiRepository by inject()

    override suspend fun data(request: ComplicationRequest): NextSessionComplicationData {
        val timeZone = repository.timeZone
        val today = Clock.System.todayIn(timeZone)
        val todaysSessions = repository.sessionsMap.first()[today].orEmpty()

        val now = Clock.System.now().toLocalDateTime(timeZone)

        val nextSessionTime = todaysSessions.map { it.startsAt }
            .filter { it > now }
            .minOrNull()

        val nextSession = todaysSessions.find { it.startsAt == nextSessionTime }

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