package dev.johnoreilly.confetti.livery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import dev.johnoreilly.confetti.analytics.AnalyticsEvent
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.isService
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import dev.johnoreilly.confetti.ui.components.NativeComponents
import dev.johnoreilly.confetti.ui.components.SessionCardModel
import dev.johnoreilly.confetti.ui.components.SpeakerCardModel
import dev.johnoreilly.confetti.ui.sessions.SessionItemView

/*
 * The sdui experiment around shared's ConferenceComponents: which arm each conference draws with,
 * and the experiment's analytics for what Confetti draws itself. Only the sduiDebug build has it.
 */

/**
 * How a native composable reports what the user did, under the contract's action names: a tap on
 * Confetti's own session row is `open`, the same as a tap on a design's card, so both arms of the
 * [SduiExperiment] are counted alike. [None] when nothing is measured.
 */
fun interface SlotTracker {
    fun action(action: Action, args: Map<String, String>)

    fun action(action: Action) = action(action, emptyMap())

    companion object {
        val None = SlotTracker { _, _ -> }
    }
}

/**
 * Confetti's own composables ([NativeComponents]), with each session card reporting its impression
 * and taps as the same analytics events a Livery design does (`livery_impression`, `livery_action`
 * with `design_id=native` and `exp_sdui=<arm>`), without loading Livery. Breaks are never counted.
 */
open class TrackedNativeComponents(
    /** Null: nothing is reported (previews). */
    private val tracking: SduiTracking? = null,
    /** The arm this instance draws for: native itself, or livery's fallback where it has no design. */
    private val arm: String = SduiExperiment.NATIVE,
) : NativeComponents() {
    @Composable
    override fun SessionCard(card: SessionCardModel) {
        val session = card.session
        if (tracking == null || session.isBreak() || session.isService()) return SessionCard(card, SlotTracker.None)
        val tracker = tracking.tracker(card.conference, SessionContract, arm)
        // Once per session shown: a lazy list reuses a row for another session.
        LaunchedEffect(card.conference, session.id) { tracking.impression(card.conference, SessionContract, arm) }
        SessionCard(card, tracker)
    }

    /** The row itself, reporting through [tracker]: also what Livery draws without a design. */
    @Composable
    fun SessionCard(card: SessionCardModel, tracker: SlotTracker) {
        SessionItemView(
            session = card.session,
            sessionSelected = { tracker.action(SessionContract.open); card.sessionSelected(it) },
            isBookmarked = card.isBookmarked,
            addBookmark = card.addBookmark,
            removeBookmark = card.removeBookmark,
            onNavigateToSignIn = card.onNavigateToSignIn,
            isLoggedIn = card.isLoggedIn,
            trackColor = card.trackColor,
            tags = card.tags,
            // Every tap, as a design's `bookmark` action is: signed out included (it then prompts).
            onBookmarkTap = { tracker.action(SessionContract.bookmark) },
        )
    }
}

/**
 * The user's arm of the [SduiExperiment] per conference series: `native` or `livery`. Decided
 * before anything is drawn (Firebase Remote Config on Android), so a native arm never builds
 * Livery. Null means the series isn't in the experiment: it draws [SduiExperiment.LIVERY].
 */
fun interface SduiArms {
    fun arm(series: String): String?
}

/**
 * Per conference, the implementation its arm names. The Livery side is built on first use, so a
 * session where every conference is native never constructs a repository, reads a bundled
 * catalog or touches the network for templates.
 */
class SduiComponents(
    private val arms: SduiArms,
    private val native: ConferenceComponents,
    livery: () -> ConferenceComponents,
    /** Conferences outside the experiment: drawn natively, never counted in either arm. */
    private val untracked: ConferenceComponents = NativeComponents(),
) : ConferenceComponents {
    private val liveryLazy = lazy(livery)

    /** Whether the Livery side has been built (tests: the native arm must never build it). */
    val liveryStarted: Boolean get() = liveryLazy.isInitialized()

    /** The conference's arm, or null when it has no series and so isn't in the experiment. */
    fun armFor(conference: String): String? {
        val series = ConferenceSeries.of(conference) ?: return null
        return arms.arm(series) ?: SduiExperiment.LIVERY
    }

    private fun pick(conference: String): ConferenceComponents = when (armFor(conference)) {
        SduiExperiment.LIVERY -> liveryLazy.value
        null -> untracked
        else -> native
    }

    @Composable
    override fun ConferenceTheme(conference: String, themeColor: String?, content: @Composable () -> Unit) =
        pick(conference).ConferenceTheme(conference, themeColor, content)

    override fun hasConferenceHeader(conference: String) = pick(conference).hasConferenceHeader(conference)

    @Composable
    override fun ConferenceHeader(state: SessionsUiState.Success) = pick(state.conference).ConferenceHeader(state)

    @Composable
    override fun SessionCard(card: SessionCardModel) = pick(card.conference).SessionCard(card)

    @Composable
    override fun SpeakerCard(card: SpeakerCardModel) = pick(card.conference).SpeakerCard(card)

    @Composable
    override fun SectionHeader(conference: String, text: String, icon: ImageVector) =
        pick(conference).SectionHeader(conference, text, icon)
}

/**
 * The [SduiExperiment]'s events for what Confetti draws itself, in the shape Livery's
 * `AnalyticsEventSink` gives its own (`livery_<type>`, `design_id`, `action`, `exp_<id>`), so a
 * dashboard compares the arms on one set of names. Through Confetti's [AnalyticsLogger]: Firebase
 * Analytics in release, logcat in debug.
 */
class SduiTracking(private val analytics: AnalyticsLogger) {
    fun impression(conference: String, contract: ConfettiContract, arm: String) =
        log("livery_impression", conference, contract, arm, emptyMap())

    fun tracker(conference: String, contract: ConfettiContract, arm: String) = SlotTracker { action, args ->
        require(contract.actions[action.name] === action) { "${action.name} is not an action of ${contract.ref}" }
        // Only an argument the contract declares reaches analytics, as with a template (PROTOCOL §7).
        log("livery_action", conference, contract, arm, mapOf("action" to action.name) +
            args.filterKeys { it == action.arg }.mapKeys { "arg_${it.key}" })
    }

    private fun log(name: String, conference: String, contract: ConfettiContract, arm: String, extra: Map<String, String>) {
        analytics.logEvent(SduiEvent(name, buildMap {
            put("design_id", SduiExperiment.NATIVE_DESIGN)
            put("contract", contract.ref)
            put("conference", conference)
            put("exp_${SduiExperiment.ID}", arm)
            putAll(extra)
        }))
    }
}

/** One analytics event of the [SduiExperiment]. */
data class SduiEvent(override val id: String, override val properties: Map<String, Any>) : AnalyticsEvent
