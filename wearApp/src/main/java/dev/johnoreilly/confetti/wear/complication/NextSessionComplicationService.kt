@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.complication

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import androidx.core.net.toUri
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.apollographql.cache.normalized.FetchPolicy
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.datalayer.watch.WearDataLayerAppHelper
import com.google.android.horologist.tiles.complication.DataComplicationService
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.android.ext.android.inject
import java.time.Instant

class NextSessionComplicationService :
    DataComplicationService<NextSessionComplicationData, NextSessionTemplate>() {
    override val renderer: NextSessionTemplate = NextSessionTemplate(this)

    private val repository: ConfettiRepository by inject()

    private val phoneSettingsSync: PhoneSettingsSync by inject()

    private val authentication: Authentication by inject()

    private val wearAppHelper: WearDataLayerAppHelper by inject()

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        runBlocking {
            wearAppHelper.markComplicationAsActivated(this@NextSessionComplicationService.javaClass.name, complicationInstanceId, type)
        }
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        runBlocking {
            wearAppHelper.markComplicationAsDeactivated(complicationInstanceId)
        }
    }

    override suspend fun data(request: ComplicationRequest): NextSessionComplicationData {
        val conference = phoneSettingsSync.conferenceFlow.first().conference
        val user = authentication.currentUser.value

        if (conference.isBlank()) {
            return NextSessionComplicationData(launchIntent = conferencesIntent())
        }

        if (user != null) {
            val responseData = repository.bookmarkedSessionsQuery(
                conference, user.uid, user, FetchPolicy.CacheOnly
            ).execute().data

            if (responseData != null) {
                val timeZone = responseData.config.timezone.toTimeZone()
                val now = Instant.now().toKotlinInstant().toLocalDateTime(timeZone)

                val bookmarks =
                    responseData.bookmarkConnection?.nodes?.map { it.sessionDetails }?.filter {
                        it.startsAt > now
                    }?.sortedBy { it.startsAt }.orEmpty()

                val sessionDetails = bookmarks.firstOrNull()
                return NextSessionComplicationData(
                    conference = responseData.config,
                    sessionDetails = sessionDetails,
                    launchIntent = if (sessionDetails != null) sessionIntent(
                        responseData.config.id,
                        sessionDetails
                    ) else conferenceIntent(conference)
                )
            }
        }

        return NextSessionComplicationData(launchIntent = conferenceIntent(conference))
    }

    private fun sessionIntent(conference: String, sessionDetails: SessionDetails): PendingIntent? {
        val sessionDetailIntent = Intent(
            Intent.ACTION_VIEW,
            "confetti://confetti/session/${conference}/${sessionDetails.id}".toUri()
        )

        return PendingIntent.getActivity(
            this,
            0,
            sessionDetailIntent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
    }

    private fun conferencesIntent(): PendingIntent? {
        val appIntent = Intent(
            Intent.ACTION_VIEW,
            "confetti://confetti/conferences".toUri()
        )

        return PendingIntent.getActivity(
            this,
            0,
            appIntent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
    }

    private fun conferenceIntent(conference: String): PendingIntent? {
        val appIntent = Intent(
            Intent.ACTION_VIEW,
            "confetti://confetti/home/${conference}".toUri()
        )

        return PendingIntent.getActivity(
            this,
            0,
            appIntent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
    }

    override fun previewData(type: ComplicationType): NextSessionComplicationData =
        renderer.previewData()
}