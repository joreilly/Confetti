package dev.johnoreilly.confetti.wear.sdui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.livery.ConferenceSeries
import dev.johnoreilly.confetti.livery.SduiArms
import dev.johnoreilly.confetti.livery.SduiExperiment
import dev.johnoreilly.confetti.livery.SectionCard
import dev.johnoreilly.confetti.livery.SessionContract
import dev.johnoreilly.confetti.ui.livery.ConferenceSlot
import dev.johnoreilly.confetti.ui.livery.LiveryStyling
import dev.johnoreilly.confetti.ui.livery.LocalSlotsDark
import dev.johnoreilly.confetti.ui.livery.toSessionCard
import dev.johnoreilly.confetti.ui.livery.toSessionHeaderCard
import dev.johnoreilly.confetti.ui.livery.toSpeakerHeaderCard
import dev.johnoreilly.confetti.wear.components.WearComponents
import kotlinx.datetime.LocalDateTime

/**
 * Each conference's Wear designs as the watch's [WearComponents]: Livery slots asking only for the
 * series' `device=wear` designs (its Wear variants, drawn with Wear's Remote Material 3), in its
 * dark theme pack (a watch is dark). A phone design is never a fallback on the watch: where the
 * series has no Wear design for a slot, [native] (Confetti's own composable) draws.
 */
class WearLiveryComponents(
    private val styling: LiveryStyling,
    private val native: WearComponents,
) : WearComponents {
    @Composable
    override fun SessionCard(
        conference: String,
        session: SessionDetails,
        currentTime: LocalDateTime?,
        isBookmarked: Boolean,
        onOpen: () -> Unit,
        onToggleBookmark: (() -> Unit)?,
        modifier: Modifier,
        native: @Composable () -> Unit,
    ) = Wear {
        ConferenceSlot(
            styling = styling,
            conference = conference,
            data = session.toSessionCard(currentTime, isBookmarked),
            modifier = modifier,
            identity = session.id,
            specific = WEAR,
            seriesDefault = false,
            actions = mapOf(
                SessionContract.open to { _ -> onOpen() },
                SessionContract.bookmark to { _ -> onToggleBookmark?.invoke() },
            ),
        ) { _ -> native() }
    }

    @Composable
    override fun SectionHeader(conference: String, text: String, modifier: Modifier, native: @Composable () -> Unit) = Wear {
        ConferenceSlot(styling, conference, SectionCard(text), modifier, identity = text, specific = WEAR, seriesDefault = false) { _ -> native() }
    }

    @Composable
    override fun SessionHeader(conference: String, session: SessionDetails, modifier: Modifier, native: @Composable () -> Unit) = Wear {
        // The watch has no venue map: the room is text, never a link.
        ConferenceSlot(
            styling, conference, session.toSessionHeaderCard(roomLink = false), modifier,
            identity = session.id, specific = WEAR, seriesDefault = false,
        ) { _ -> native() }
    }

    @Composable
    override fun SpeakerHeader(conference: String, speaker: SpeakerDetails, modifier: Modifier, native: @Composable () -> Unit) = Wear {
        ConferenceSlot(
            styling, conference, speaker.toSpeakerHeaderCard(), modifier,
            identity = speaker.id, specific = WEAR, seriesDefault = false,
        ) { _ -> native() }
    }

    /** The slots in the series' dark pack: a watch has no phone `MaterialTheme` to judge it from. */
    @Composable
    private fun Wear(content: @Composable () -> Unit) = CompositionLocalProvider(LocalSlotsDark provides true, content = content)

    private companion object {
        /** A watch's designs: tagged for the device, beside their series. */
        val WEAR = listOf(mapOf("device" to "wear"))
    }
}

/**
 * Per conference, the implementation its arm of the sdui experiment names, as the phone's
 * SduiComponents: the series' `sdui_<series>` arm, the base catalog's for any other conference.
 */
class WearSduiComponents(
    private val arms: SduiArms,
    private val native: WearComponents,
    livery: () -> WearComponents,
) : WearComponents {
    private val liveryLazy = lazy(livery)

    /** Whether the Livery side has been built (tests: the native arm must never build it). */
    val liveryStarted: Boolean get() = liveryLazy.isInitialized()

    private fun pick(conference: String): WearComponents {
        val series = ConferenceSeries.of(conference) ?: ConferenceSeries.BASE
        return if ((arms.arm(series) ?: SduiExperiment.LIVERY) == SduiExperiment.LIVERY) liveryLazy.value else native
    }

    @Composable
    override fun SessionCard(
        conference: String,
        session: SessionDetails,
        currentTime: LocalDateTime?,
        isBookmarked: Boolean,
        onOpen: () -> Unit,
        onToggleBookmark: (() -> Unit)?,
        modifier: Modifier,
        native: @Composable () -> Unit,
    ) = pick(conference).SessionCard(conference, session, currentTime, isBookmarked, onOpen, onToggleBookmark, modifier, native)

    @Composable
    override fun SectionHeader(conference: String, text: String, modifier: Modifier, native: @Composable () -> Unit) =
        pick(conference).SectionHeader(conference, text, modifier, native)

    @Composable
    override fun SessionHeader(conference: String, session: SessionDetails, modifier: Modifier, native: @Composable () -> Unit) =
        pick(conference).SessionHeader(conference, session, modifier, native)

    @Composable
    override fun SpeakerHeader(conference: String, speaker: SpeakerDetails, modifier: Modifier, native: @Composable () -> Unit) =
        pick(conference).SpeakerHeader(conference, speaker, modifier, native)
}
