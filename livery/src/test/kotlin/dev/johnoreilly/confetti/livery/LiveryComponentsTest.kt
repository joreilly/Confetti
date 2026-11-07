package dev.johnoreilly.confetti.livery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.johnoreilly.confetti.analytics.AnalyticsEvent
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.components.ConferenceComponentsFactory
import dev.johnoreilly.confetti.ui.components.NativeComponents
import dev.johnoreilly.confetti.ui.components.SduiComponents
import dev.johnoreilly.confetti.ui.components.SduiTracking
import dev.johnoreilly.confetti.ui.components.SessionCardModel
import dev.johnoreilly.confetti.ui.livery.ConferenceSlot
import dev.johnoreilly.confetti.ui.livery.LiveryComponents
import dev.johnoreilly.confetti.ui.livery.toSessionCard
import kotlinx.datetime.LocalDateTime
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The conference components as the app's screens use them, before any templates exist: every
 * slot draws Confetti's own row, tracked as the experiment's events, and the native arm never
 * builds Livery.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LiveryComponentsTest {
    @get:Rule val compose = createComposeRule()

    private val live = LocalDateTime.parse("2023-04-13T14:10")
    private val logged = mutableListOf<AnalyticsEvent>()
    private val analytics = object : AnalyticsLogger {
        override fun logEvent(event: AnalyticsEvent) { synchronized(logged) { logged += event } }
    }

    private fun livery() = LiveryComponents(
        confettiLiveryStyling(ApplicationProvider.getApplicationContext(), liveryHttpClient(OkHttpClient()), analytics, refresh = false),
        NativeComponents(SduiTracking(analytics), SduiExperiment.LIVERY),
    )

    /** The app finds this module through ServiceLoader, as only the sduiDebug variant can. */
    @Test fun registeredAsTheVariantsFactory() {
        assertTrue(ConferenceComponentsFactory.load() is LiveryComponentsFactory)
    }

    /** Remote Config's kill switch: the app's real components, with Livery never built. */
    @Test fun theKillSwitchNeverBuildsLivery() {
        val off = FirebaseSduiArms(config = { mapOf(FirebaseSduiArms.KILL_SWITCH to "false")[it] })
        val components = confettiComponents(
            ApplicationProvider.getApplicationContext(), OkHttpClient(), analytics, arms = off,
        )
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Column {
                    for (conference in listOf("kotlinconf2026", "droidconlondon2025", "fosdem2025")) {
                        components.SessionCard(card(conference))
                    }
                }
            }
        }
        compose.waitForIdle()
        assertEquals(3, nativeRows())
        assertEquals(0, compose.onAllNodes(liverySlot).fetchSemanticsNodes().size)
        assertFalse(components.liveryStarted)
    }

    @Test fun nativeArmNeverBuildsLivery() {
        val components = SduiComponents(
            arms = { SduiExperiment.NATIVE },
            native = NativeComponents(SduiTracking(analytics), SduiExperiment.NATIVE),
            livery = { error("the native arm must not build Livery") },
        )
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Column {
                    for (conference in listOf("kotlinconf2026", "droidconlondon2025")) components.SessionCard(card(conference))
                    components.Promo("home.feed", "droidconlondon2026", Modifier.fillMaxWidth())
                }
            }
        }
        compose.waitForIdle()
        assertEquals(2, nativeRows())
        compose.onAllNodesWithText(sessionDetails.title)[0].performClick()
        compose.waitForIdle()
        assertFalse(components.liveryStarted)
        val events = synchronized(logged) { logged.toList() }
        assertEquals(2, events.count { it.id == "livery_impression" })
        val tap = events.single { it.id == "livery_action" }
        assertEquals("open", tap.properties["action"])
        assertEquals(SduiExperiment.NATIVE, tap.properties["exp_${SduiExperiment.ID}"])
        assertEquals(SduiExperiment.NATIVE_DESIGN, tap.properties["design_id"])
    }

    /** A conference with no series draws native rows but is counted in neither arm. */
    @Test fun aConferenceOutsideTheExperimentIsNotCounted() {
        val components = SduiComponents(
            arms = { SduiExperiment.NATIVE },
            native = NativeComponents(SduiTracking(analytics), SduiExperiment.NATIVE),
            livery = { error("not in the experiment") },
        )
        compose.setContent { MaterialTheme(colorScheme = lightColorScheme()) { components.SessionCard(card("fosdem2025")) } }
        compose.waitForIdle()
        assertEquals(1, nativeRows())
        assertEquals(0, synchronized(logged) { logged.count { it.id == "livery_impression" } })
    }

    /** With no catalog yet, the livery arm builds Livery and draws Confetti's own rows. */
    @Test fun liveryArmWithoutTemplatesDrawsNativeRows() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Column {
                    components.SessionCard(card("kotlinconf2026"))
                    components.SessionCard(card("droidconlondon2025"))
                }
            }
        }
        compose.waitForIdle()
        assertTrue(components.liveryStarted)
        assertEquals(2, nativeRows())
        assertEquals(0, compose.onAllNodes(liverySlot).fetchSemanticsNodes().size)
        // Those rows count as impressions in the livery arm, as the native arm's rows do.
        compose.waitUntil(10_000) { synchronized(logged) { logged.count { it.id == "livery_impression" } } == 2 }
        val impression = synchronized(logged) { logged.first { it.id == "livery_impression" } }
        assertEquals(SduiExperiment.NATIVE_DESIGN, impression.properties["design_id"])
        assertEquals(SduiExperiment.LIVERY, impression.properties["exp_${SduiExperiment.ID}"])
    }

    /**
     * A slot Confetti draws nothing for (the header) never counts a native impression: native
     * impressions match the native rows on screen, whether or not the catalog has templates.
     */
    @Test fun anEmptyFallbackCountsNoImpression() {
        val styling = confettiLiveryStyling(ApplicationProvider.getApplicationContext(), liveryHttpClient(OkHttpClient()), analytics, refresh = false)
        compose.setContent {
            Column {
                ConferenceSlot(styling, "droidconlondon2025", ConferenceCard(name = "droidcon London", dates = "29-30 Oct"), fallback = null)
                ConferenceSlot(styling, "droidconlondon2025", sessionDetails.toSessionCard(live, isBookmarked = false), identity = "s1") {
                    BasicText("native row")
                }
            }
        }
        // Either the session's template or its native row, once the slot has planned.
        compose.waitUntil(10_000) { synchronized(logged) { logged.any { it.id == "livery_impression" } } }
        Thread.sleep(200)
        compose.waitForIdle()
        val nativeRows = compose.onAllNodesWithText("native row").fetchSemanticsNodes().size
        val nativeImpressions = synchronized(logged) {
            logged.count { it.id == "livery_impression" && it.properties["design_id"] == SduiExperiment.NATIVE_DESIGN }
        }
        assertEquals(nativeRows, nativeImpressions)
    }

    /** A row a lazy list reuses for another session reports that session's impression too. */
    @Test fun anImpressionPerSessionShown() {
        val native = NativeComponents(SduiTracking(analytics), SduiExperiment.NATIVE)
        var session by mutableStateOf(sessionDetails)
        compose.setContent { native.SessionCard(card("kotlinconf2026", session)) }
        fun impressions() = synchronized(logged) { logged.count { it.id == "livery_impression" } }
        compose.waitUntil(10_000) { impressions() == 1 }
        session = sessionDetails.copy(id = "another-session", title = "Another session")
        compose.waitForIdle()
        compose.waitUntil(10_000) { impressions() == 2 }
    }

    /** A signed-out bookmark tap counts, as a design's `bookmark` action does. */
    @Test fun signedOutBookmarkTapsCount() {
        val native = NativeComponents(SduiTracking(analytics), SduiExperiment.NATIVE)
        val signedOut = card("kotlinconf2026").let {
            SessionCardModel(it.conference, it.session, it.now, false, false, null, emptyList(), {}, {}, {}, {})
        }
        compose.setContent { MaterialTheme { native.SessionCard(signedOut) } }
        compose.onNodeWithContentDescription("add bookmark").performClick()
        compose.waitForIdle()
        val tap = synchronized(logged) { logged.single { it.id == "livery_action" } }
        assertEquals("bookmark", tap.properties["action"])
    }

    private fun card(conference: String, session: SessionDetails = sessionDetails) = SessionCardModel(
        conference = conference, session = session, now = live, isBookmarked = false, isLoggedIn = true,
        trackColor = null, tags = emptyList(), sessionSelected = {}, addBookmark = {}, removeBookmark = {}, onNavigateToSignIn = {},
    )

    /** Confetti's own rows: the title as text outside any template. */
    private fun nativeRows() =
        compose.onAllNodes(hasText(sessionDetails.title) and !hasAnyAncestor(liverySlot)).fetchSemanticsNodes().size

    /** A node a Livery template drew: LiveryHost tags each player `livery:<placement or contract>:<design>`. */
    private val liverySlot = SemanticsMatcher("is a Livery slot") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("livery:") == true
    }
}
