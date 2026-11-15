package dev.johnoreilly.confetti.wear.sdui

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.core.WidgetInstanceId
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.core.ContainerInfo
import androidx.test.core.app.ApplicationProvider
import dev.johnoreilly.confetti.livery.confettiLiveryStyling
import dev.johnoreilly.confetti.livery.liveryHttpClient
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.livery.LiveryStyling
import dev.johnoreilly.confetti.wear.sdui.widget.NextSession
import dev.johnoreilly.confetti.wear.sdui.widget.NextSessionSource
import dev.johnoreilly.confetti.wear.sdui.widget.NextSessionWidget
import ee.schimke.composeai.rcwidget.RcWearHost
import ee.schimke.livery.client.PlanResult
import ee.schimke.livery.instantiate.WidgetTap
import ee.schimke.livery.widgets.wear.WearWidgetSlot
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * The next-session widget over the bundled catalogs: each series' session design in the watch's
 * container (small and large), written into a Glance Wear document; a line of text when there is
 * nothing to show; and a tap that opens the session it drew. A plain Application: Koin holds only
 * what the widget asks for.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class NextSessionWidgetTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val now = LocalDateTime.parse("2023-04-13T14:10")
    private var next: NextSession = NextSession.Session("droidconlondon2025", sessionDetails, now)

    @Before fun koin() {
        startKoin {
            modules(module {
                single<LiveryStyling> { confettiLiveryStyling(context, liveryHttpClient(OkHttpClient()), refresh = false) }
                single<NextSessionSource> { NextSessionSource { next } }
            })
        }
    }

    @After fun stop() = stopKoin()

    /** The widget, with what a test needs of it open: its plan, its taps, and why it fell back. */
    private class Widget : NextSessionWidget() {
        val fallbacks = mutableListOf<String>()

        fun slot(context: Context, small: Boolean, instance: Int = 1): WearWidgetSlot {
            val container = if (small) WearWidgetSlot.CONTAINER_SMALL else WearWidgetSlot.CONTAINER_LARGE
            val frame = if (small) RcWearHost.Pixel.small else RcWearHost.Pixel.large
            return WearWidgetSlot(instance, frame, capabilities(context, container))
        }

        suspend fun planFor(context: Context, slot: WearWidgetSlot) = plan(context, slot)

        fun tap(context: Context, instance: Int): PendingIntent = tapIntent(context, instance, 0, WidgetTap("open", emptyMap()))

        override fun fallback(context: Context, slot: WearWidgetSlot, reason: String): WearWidgetData {
            fallbacks += reason
            return super.fallback(context, slot, reason)
        }
    }

    private fun params(small: Boolean, instance: Int = 1): WearWidgetParams {
        val frame = if (small) RcWearHost.Pixel.small else RcWearHost.Pixel.large
        return WearWidgetParams(
            WidgetInstanceId("test", instance),
            if (small) ContainerInfo.CONTAINER_TYPE_SMALL else ContainerInfo.CONTAINER_TYPE_LARGE,
            frame.widthDp, frame.heightDp, frame.horizontalPaddingDp, frame.verticalPaddingDp, frame.cornerRadiusDp,
        )
    }

    /** Each series' session design draws the widget, in the container the watch reports. */
    @Test fun eachSeriesDrawsItsSessionDesignInEachContainer() = runBlocking {
        val widget = Widget()
        for ((conference, design) in listOf(
            "droidconlondon2025" to "droidcon-session",
            "kotlinconf2026" to "kotlinconf-session",
            "fosdem2025" to "confetti-session",
        )) {
            next = NextSession.Session(conference, sessionDetails, now)
            for (small in listOf(true, false)) {
                val plan = widget.planFor(context, widget.slot(context, small))
                assertTrue("$conference: $plan", plan is PlanResult.Ready)
                val template = (plan as PlanResult.Ready).plan.template
                assertEquals(design, template.design?.id)
                assertEquals(if (small) "wear:small" else "wear:large", template.entry.target.container)
            }
        }
    }

    /** The planned template is written into a Glance Wear document for the watch, not its fallback. */
    @Test fun theTemplateIsInstantiatedForTheWatch() = runBlocking {
        for (small in listOf(true, false)) {
            val widget = Widget()
            val params = params(small)
            val data = widget.provideWidgetData(context, params)
            val content = data.captureRawContent(context, params, false)
            assertTrue("a document", content.rcDocument.isNotEmpty())
            assertEquals("no fallback", emptyList<String>(), widget.fallbacks)
        }
    }

    /** Signed out, no conference, nothing ahead: a line of text, never an empty widget. */
    @Test fun nothingToShowDrawsTheFallback() = runBlocking {
        for (reason in listOf(NextSession.NOTHING_AHEAD, NextSession.SIGNED_OUT, NextSession.NO_CONFERENCE)) {
            next = NextSession.Nothing(reason)
            val widget = Widget()
            val params = params(small = true)
            widget.provideWidgetData(context, params).captureRawContent(context, params, false)
            assertEquals(listOf(reason), widget.fallbacks)
        }
    }

    /** A tap opens the session the widget drew. */
    @Test fun aTapOpensTheSessionItDrew() = runBlocking {
        val widget = Widget()
        widget.planFor(context, widget.slot(context, small = false, instance = 7))
        val intent = shadowOf(widget.tap(context, 7)).savedIntent
        assertEquals("confetti://confetti/session/droidconlondon2025/${sessionDetails.id}", intent.data.toString())
        assertEquals(context.packageName, intent.`package`)
    }
}
