package dev.johnoreilly.confetti.wear.sdui

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.foundation.text.BasicText
import androidx.test.core.app.ApplicationProvider
import dev.johnoreilly.confetti.livery.SduiExperiment
import dev.johnoreilly.confetti.livery.confettiLiveryStyling
import dev.johnoreilly.confetti.livery.liveryHttpClient
import dev.johnoreilly.confetti.preview.johnOreillySpeaker
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.wear.components.NativeWearComponents
import kotlinx.datetime.LocalDateTime
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The Wear sdui build's components over the bundled catalogs: each conference's Wear variants
 * (`device=wear`, drawn with Wear's Remote Material 3) on the watch, never its phone designs, and
 * Confetti's own composables where a series has none. A plain Application: Koin and Firebase are
 * the app's, not what's under test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WearLiveryComponentsTest {
    @get:Rule val compose = createComposeRule()

    private val now = LocalDateTime.parse("2023-04-13T14:10")

    private fun livery() = WearLiveryComponents(
        confettiLiveryStyling(ApplicationProvider.getApplicationContext(), liveryHttpClient(OkHttpClient()), refresh = false),
        native = NativeWearComponents,
    )

    /** A node a Livery template drew: LiveryHost tags each player `livery:<contract>:<design>`. */
    private val liverySlot = SemanticsMatcher("is a Livery slot") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("livery:") == true
    }

    private fun designs() = compose.onAllNodes(liverySlot).fetchSemanticsNodes().map { it.config[SemanticsProperties.TestTag].substringAfterLast(':') }

    /** Each series' Wear session card, and the base catalog's for a conference with no series. */
    @Test fun sessionCardsDrawEachSeriesWearVariant() {
        val components = livery()
        compose.setContent {
            Column {
                for (conference in listOf("droidconlondon2025", "kotlinconf2026", "fosdem2025")) {
                    components.SessionCard(conference, sessionDetails, now, false, {}, {}, Modifier.fillMaxWidth()) { BasicText("native") }
                }
            }
        }
        compose.waitUntil(10_000) { designs().size == 3 }
        assertEquals(listOf("droidcon-session-wear", "kotlinconf-session-wear", "confetti-session-wear"), designs())
    }

    /** The star in a Wear card is the bookmark: the card's own tap still opens the session. */
    @Test fun theStarTogglesTheBookmark() {
        val components = livery()
        var toggles = 0
        var opens = 0
        compose.setContent {
            components.SessionCard("droidconlondon2025", sessionDetails, now, false, { opens++ }, { toggles++ }, Modifier.fillMaxWidth()) { BasicText("native") }
        }
        val star = hasText("☆") and hasAnyAncestor(liverySlot)
        compose.waitUntil(10_000) { compose.onAllNodes(star).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(star).performClick()
        compose.waitUntil(10_000) { toggles == 1 }
        assertEquals(0, opens)
    }

    /** Every Wear slot draws its series' Wear design. */
    @Test fun everySlotDrawsItsWearDesign() {
        val components = livery()
        compose.setContent {
            Column {
                components.SectionHeader("kotlinconf2026", "10:30", Modifier.fillMaxWidth()) { BasicText("native") }
                components.SessionHeader("kotlinconf2026", sessionDetails, Modifier.fillMaxWidth()) { BasicText("native") }
                components.SpeakerHeader("kotlinconf2026", johnOreillySpeaker, Modifier.fillMaxWidth()) { BasicText("native") }
            }
        }
        compose.waitUntil(10_000) { designs().size == 3 }
        assertEquals(listOf("kotlinconf-section-wear", "kotlinconf-session-header-wear", "kotlinconf-speaker-header-wear"), designs())
    }

    /** A conference in the native arm draws Confetti's own, and never builds Livery. */
    @Test fun theNativeArmNeverBuildsLivery() {
        val components = WearSduiComponents(arms = { SduiExperiment.NATIVE }, native = NativeWearComponents) {
            error("the native arm must not build Livery")
        }
        compose.setContent {
            components.SessionCard("droidconlondon2025", sessionDetails, now, false, {}, {}, Modifier.fillMaxWidth()) { BasicText("native") }
        }
        compose.waitForIdle()
        assertEquals(1, compose.onAllNodes(hasText("native")).fetchSemanticsNodes().size)
        assertFalse(components.liveryStarted)
    }
}
