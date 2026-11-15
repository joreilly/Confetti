package dev.johnoreilly.confetti.wear.sdui.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteText
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.glance.wear.AssociateWithGlanceWearWidget
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import com.apollographql.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.livery.ConferenceSeries
import dev.johnoreilly.confetti.livery.SessionContract
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.ui.livery.LiveryStyling
import dev.johnoreilly.confetti.ui.livery.toSessionCard
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import ee.schimke.livery.client.Bindings
import ee.schimke.livery.client.ImageResolver
import ee.schimke.livery.client.PlanResult
import ee.schimke.livery.client.TemplateQuery
import ee.schimke.livery.instantiate.WidgetTap
import ee.schimke.livery.widgets.wear.LiveryWearWidget
import ee.schimke.livery.widgets.wear.WearWidgetSlot
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock

/**
 * The next session the user bookmarked (or the one on now), as a Glance Wear widget drawn from its
 * conference series' Livery catalog: the series' session design's `wear:small` or `wear:large`
 * template, in its dark pack. A tap opens the session.
 *
 * It draws only what is on the watch already (Livery never waits on the network for a widget): the
 * bookmarks from Apollo's cache, the templates from the APK's bundled catalogs or a refreshed copy.
 * With nothing to show (no conference, signed out, nothing bookmarked ahead) it draws a line of
 * text saying so. The session comes from [NextSessionSource] (the user's bookmarks, [BookmarkedNextSession]).
 *
 * Glance Wear's picker offers one container type per service, so each container has its own
 * subclass and service ([NextSessionSmallWidget], [NextSessionLargeWidget]); the template is
 * chosen per render from the container the watch reports.
 */
abstract class NextSessionWidget : LiveryWearWidget(), KoinComponent {
    private val styling: LiveryStyling by inject()
    private val sessions: NextSessionSource by inject()

    override suspend fun plan(context: Context, slot: WearWidgetSlot): PlanResult {
        val next = when (val n = sessions.next()) {
            is NextSession.Session -> n
            is NextSession.Nothing -> return PlanResult.Fallback(n.reason)
        }
        val catalog = styling.catalog(next.conference) ?: return PlanResult.Fallback("no catalog for ${next.conference}")
        shown[slot.instanceId] = next.conference to next.session.id
        // The series' session design for this container, as the app's own slots ask (by series tag).
        val query = TemplateQuery(
            SessionContract.ref, slot.caps,
            widthDp = slot.frame.widthDp.toInt(), heightDp = slot.frame.heightDp.toInt(),
            tags = mapOf(ConferenceSeries.TAG to catalog.series), minor = SessionContract.minor,
        )
        return catalog.planner.forAppData(
            query,
            next.session.toSessionCard(next.now, isBookmarked = true).toJson(),
            // A widget's theme is baked in: the series' dark pack, as a watch is dark.
            extra = catalog.theme(dark = true)() ?: Bindings.EMPTY,
        )
    }

    // The session design draws no images; every catalog shares the app's image rules.
    override fun images(context: Context): ImageResolver =
        styling.catalogs.values.firstNotNullOf { it.imageResolver }

    override fun tapIntent(context: Context, instanceId: Int, index: Int, tap: WidgetTap): PendingIntent {
        // The session contract's one widget tap is `open`, which takes no arguments: the widget
        // knows which session it drew.
        val uri = shown[instanceId]?.let { (conference, session) -> "confetti://confetti/session/$conference/$session" }
            ?: "confetti://confetti/conferences"
        return PendingIntent.getActivity(
            context, instanceId * 100 + index,
            Intent(Intent.ACTION_VIEW, uri.toUri()).setPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    override fun fallback(context: Context, slot: WearWidgetSlot, reason: String): WearWidgetData {
        val text = when (reason) {
            NextSession.NO_CONFERENCE -> "Choose a conference in Confetti"
            NextSession.SIGNED_OUT -> "Sign in to Confetti to see your bookmarks"
            NextSession.NOTHING_AHEAD -> "No bookmarked sessions coming up"
            else -> "Open Confetti"
        }
        return WearWidgetDocument(WearWidgetBrush.color(RemoteColor(Color(0xFF202124)))) {
            RemoteBox(RemoteModifier.fillMaxSize(), contentAlignment = RemoteAlignment.Center) {
                RemoteText(text.rs, color = RemoteColor(Color.White))
            }
        }
    }

    companion object {
        /** Each widget instance's last drawn session (conference, id): what its tap opens. */
        private val shown = ConcurrentHashMap<Int, Pair<String, String>>()

        /** Redraws every placed widget: after a bookmark changes, as the tile is. */
        suspend fun updateAll(context: Context) {
            NextSessionSmallWidget().triggerUpdateAll(context)
            NextSessionLargeWidget().triggerUpdateAll(context)
        }
    }
}

/** What the widget shows: the user's next session, or why there is none. */
sealed interface NextSession {
    data class Session(val conference: String, val session: SessionDetails, val now: LocalDateTime) : NextSession
    data class Nothing(val reason: String) : NextSession

    companion object {
        const val NO_CONFERENCE = "no conference"
        const val SIGNED_OUT = "signed out"
        const val NOTHING_AHEAD = "no upcoming bookmark"
    }
}

/** Where the widget's session comes from (the user's bookmarks; a test's fixture). */
fun interface NextSessionSource {
    suspend fun next(): NextSession
}

/**
 * The bookmarked session on now, else the next to start, from Apollo's cache (as the tile reads
 * it): a widget never waits on the network.
 */
class BookmarkedNextSession(
    private val repository: ConfettiRepository,
    private val settings: PhoneSettingsSync,
    private val authentication: Authentication,
) : NextSessionSource {
    override suspend fun next(): NextSession {
        val conference = settings.conferenceFlow.first().conference
        if (conference.isBlank()) return NextSession.Nothing(NextSession.NO_CONFERENCE)
        val user = authentication.currentUser.value ?: return NextSession.Nothing(NextSession.SIGNED_OUT)
        val data = repository.bookmarkedSessionsQuery(conference, user.uid, user, FetchPolicy.CacheOnly).execute().data
            ?: return NextSession.Nothing(NextSession.NOTHING_AHEAD)
        val now = Clock.System.now().toLocalDateTime(data.config.timezone.toTimeZone())
        val session = data.bookmarkConnection?.nodes?.map { it.sessionDetails }
            ?.filter { it.endsAt > now }?.minByOrNull { it.startsAt }
            ?: return NextSession.Nothing(NextSession.NOTHING_AHEAD)
        return NextSession.Session(conference, session, now)
    }
}

class NextSessionSmallWidget : NextSessionWidget()

class NextSessionLargeWidget : NextSessionWidget()

@AssociateWithGlanceWearWidget(NextSessionSmallWidget::class)
class NextSessionSmallWidgetService : GlanceWearWidgetService() {
    override val widget = NextSessionSmallWidget()
}

@AssociateWithGlanceWearWidget(NextSessionLargeWidget::class)
class NextSessionLargeWidgetService : GlanceWearWidgetService() {
    override val widget = NextSessionLargeWidget()
}
