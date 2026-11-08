package dev.johnoreilly.confetti.ui.livery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.isService
import dev.johnoreilly.confetti.livery.ConferenceCard
import dev.johnoreilly.confetti.livery.ConferenceContract
import dev.johnoreilly.confetti.livery.SessionCard
import dev.johnoreilly.confetti.livery.SessionContract
import dev.johnoreilly.confetti.livery.SpeakerCard
import dev.johnoreilly.confetti.livery.SpeakerContract
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import dev.johnoreilly.confetti.livery.TrackedNativeComponents
import dev.johnoreilly.confetti.ui.components.SessionCardModel
import dev.johnoreilly.confetti.ui.components.SpeakerCardModel
import dev.johnoreilly.confetti.ui.SignInDialog
import dev.johnoreilly.confetti.livery.SlotTracker
import kotlinx.datetime.LocalDateTime
import sessionTimeFormat

/*
 * Confetti's data, copied onto the contracts' data classes (livery-contracts/ContractData.kt).
 * These mappings are the only place the app's model (Apollo's fragments) meets a design, and the
 * native composables are handed a SlotTracker so both arms of the sdui experiment report taps
 * under the same contract action names.
 */

/** The conference header, from the agenda's state. */
fun SessionsUiState.Success.toConferenceCard(ticketsUrl: String? = null): ConferenceCard? {
    val dates = confDates.sorted()
    val first = dates.firstOrNull() ?: return null
    val last = dates.last()
    val dateText = if (first == last) "${first.day} ${first.month.name.take(3)} ${first.year}"
        else "${first.day}–${last.day} ${last.month.name.take(3)} ${last.year}"
    return ConferenceCard(name = conferenceName, dates = dateText, live = now.date in dates, ticketsUrl = ticketsUrl)
}

fun SessionDetails.toSessionCard(now: LocalDateTime?, isBookmarked: Boolean) = SessionCard(
    title = title,
    speakers = speakers.joinToString { it.sessionSpeakerDetails.name },
    room = room?.name,
    time = "${startsAt.sessionTimeFormat()} – ${endsAt.sessionTimeFormat()}",
    tag = tags.firstOrNull(),
    live = now != null && now >= startsAt && now < endsAt,
    bookmarked = isBookmarked,
)

fun SpeakerDetails.toSpeakerCard() = SpeakerCard(
    name = name,
    tagline = listOfNotNull(tagline, company).filter { it.isNotBlank() }.joinToString(" · "),
    photoUrl = photoUrlThumbnail ?: photoUrl,
    sessions = when (val n = sessions.size) {
        0 -> null
        1 -> "1 session"
        else -> "$n sessions"
    },
)

/**
 * The conference's Livery designs as [ConferenceComponents]: what `SduiComponents` draws for a
 * conference in the livery arm. Livery maps the agenda's header, the session cards and the speaker
 * cards; everything else is [native]'s. A mapped slot with no template for this conference (or a conference with no
 * catalog) draws [native] too, reporting its taps as `design_id=native`.
 */
class LiveryComponents(
    private val styling: LiveryStyling,
    private val native: TrackedNativeComponents,
) : ConferenceComponents by native {
    /** Native's theme, while this conference's catalog is watched for the designs it has. */
    @Composable
    override fun ConferenceTheme(conference: String, themeColor: String?, content: @Composable () -> Unit) {
        styling.catalog(conference)?.let { catalog -> LaunchedEffect(catalog) { catalog.watchDesigns() } }
        native.ConferenceTheme(conference, themeColor, content)
    }

    /** Only once the conference's catalog has a header design: the agenda adds no empty item. */
    override fun hasConferenceHeader(conference: String) =
        styling.catalog(conference)?.hasDesign(ConferenceContract) == true

    /** The header. The designs draw their tickets button only when there is a ticketsUrl (none in Confetti's data yet). */
    @Composable
    override fun ConferenceHeader(state: SessionsUiState.Success) {
        val card = state.toConferenceCard() ?: return
        val uriHandler = LocalUriHandler.current
        ConferenceSlot(
            styling = styling,
            conference = state.conference,
            data = card,
            actions = mapOf(ConferenceContract.tickets to { args -> args["url"]?.let(uriHandler::openUri) }),
            // Native draws no header, so without a template there's nothing to see or count.
            fallback = null,
        )
    }

    @Composable
    override fun SessionCard(card: SessionCardModel) {
        // Breaks are the agenda's own: Confetti's row, never a design, and never counted.
        if (card.session.isBreak() || card.session.isService()) return native.SessionCard(card, SlotTracker.None)
        // Signed out, the star asks first, as Confetti's own row does.
        var askToSignIn by remember { mutableStateOf(false) }
        ConferenceSlot(
            styling = styling,
            conference = card.conference,
            data = card.session.toSessionCard(card.now, card.isBookmarked),
            identity = card.session.id,
            actions = mapOf(
                SessionContract.open to { _ -> card.sessionSelected(card.session.id) },
                SessionContract.bookmark to { _ -> if (card.isLoggedIn) card.toggleBookmark() else askToSignIn = true },
            ),
        ) { tracker -> native.SessionCard(card, tracker) }
        if (askToSignIn) {
            SignInDialog(onDismissRequest = { askToSignIn = false }, onSignInClicked = card.onNavigateToSignIn)
        }
    }

    @Composable
    override fun SpeakerCard(card: SpeakerCardModel) = SpeakerCardSlot(
        styling = styling,
        conference = card.conference,
        speaker = card.speaker,
        onOpen = { card.speakerSelected(card.speaker.id) },
        fallback = { native.SpeakerCard(card) },
    )
}

/** A speaker, in the conference's speaker design, or [fallback]: the speakers grid's cards. */
@Composable
fun SpeakerCardSlot(
    styling: LiveryStyling,
    conference: String,
    speaker: SpeakerDetails,
    onOpen: () -> Unit = {},
    fallback: (@Composable (SlotTracker) -> Unit)? = null,
) {
    ConferenceSlot(
        styling = styling,
        conference = conference,
        data = speaker.toSpeakerCard(),
        identity = speaker.id,
        actions = mapOf(SpeakerContract.open to { _ -> onOpen() }),
        fallback = fallback,
    )
}
