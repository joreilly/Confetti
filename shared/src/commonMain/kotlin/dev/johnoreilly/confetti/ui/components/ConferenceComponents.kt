package dev.johnoreilly.confetti.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.ui.ConferenceMaterialThemeFromSettings
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.sessions.SessionItemView
import dev.johnoreilly.confetti.ui.speakers.SpeakerGridItem
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject

/**
 * What a conference can restyle: its theme, the header above its agenda, the items and section
 * headers of its lists (agenda, bookmarks, search, speakers), and the promotions placed around the
 * app. The app's composables take it as a context parameter
 * (`context(components: ConferenceComponents)`) from `App()` down, so no screen looks it up.
 *
 * Koin binds the implementation (Confetti's own [NativeComponents] by default), and each platform's
 * root reaches the app through [WithConferenceComponents]. An implementation that restyles only
 * some of these can delegate the rest to [NativeComponents].
 */
interface ConferenceComponents {
    /** The conference's colours around everything shown for [conference]. */
    @Composable
    fun ConferenceTheme(conference: String, themeColor: String?, content: @Composable () -> Unit)

    /** Whether [conference] has a header above the agenda: the agenda only adds an item for one. */
    fun hasConferenceHeader(conference: String): Boolean

    /** Above the agenda, when [hasConferenceHeader]. */
    @Composable
    fun ConferenceHeader(state: SessionsUiState.Success)

    /** One item in a list of sessions: a talk, or a break, which an implementation may draw as Confetti does. */
    @Composable
    fun SessionCard(card: SessionCardModel)

    /** One speaker in the speakers grid. */
    @Composable
    fun SpeakerCard(card: SpeakerCardModel)

    /** A list's section header: a time in the agenda and bookmarks, a result type in search. */
    @Composable
    fun SectionHeader(conference: String, text: String, icon: ImageVector)

    /**
     * Whether [conference] may show a promotion at [placement] (`home.feed`, `session.footer`,
     * `speaker.footer`): a screen only makes room for one where it may.
     */
    fun hasPromo(placement: String, conference: String): Boolean

    /** A promotion at [placement], when [hasPromo]. */
    @Composable
    fun Promo(placement: String, conference: String, modifier: Modifier)
}

/** What a session card needs, whoever draws it: the talk, its state and the list's callbacks. */
class SessionCardModel(
    val conference: String,
    val session: SessionDetails,
    /** The conference's current time, where the list tracks it (the agenda); else null. */
    val now: LocalDateTime?,
    val isBookmarked: Boolean,
    val isLoggedIn: Boolean,
    val trackColor: Color?,
    val tags: List<String>,
    val sessionSelected: (String) -> Unit,
    val addBookmark: (String) -> Unit,
    val removeBookmark: (String) -> Unit,
    val onNavigateToSignIn: () -> Unit,
    /** Opens a speaker's page by id, where the screen can (a design's speaker names); null where it can't. */
    val speakerSelected: ((String) -> Unit)? = null,
) {
    /** The bookmark star: sign in first, then add or remove. */
    fun toggleBookmark() = when {
        !isLoggedIn -> onNavigateToSignIn()
        isBookmarked -> removeBookmark(session.id)
        else -> addBookmark(session.id)
    }
}

/** What a speaker card needs, whoever draws it. */
class SpeakerCardModel(
    val conference: String,
    val speaker: SpeakerDetails,
    val speakerSelected: (String) -> Unit,
)

/** Confetti's own composables, and no agenda header or promotions. */
open class NativeComponents : ConferenceComponents {
    @Composable
    override fun ConferenceTheme(conference: String, themeColor: String?, content: @Composable () -> Unit) =
        ConferenceMaterialThemeFromSettings(themeColor, content)

    override fun hasConferenceHeader(conference: String) = false

    @Composable
    override fun ConferenceHeader(state: SessionsUiState.Success) {}

    @Composable
    override fun SessionCard(card: SessionCardModel) {
        SessionItemView(
            session = card.session,
            sessionSelected = card.sessionSelected,
            isBookmarked = card.isBookmarked,
            addBookmark = card.addBookmark,
            removeBookmark = card.removeBookmark,
            onNavigateToSignIn = card.onNavigateToSignIn,
            isLoggedIn = card.isLoggedIn,
            trackColor = card.trackColor,
            tags = card.tags,
        )
    }

    @Composable
    override fun SpeakerCard(card: SpeakerCardModel) = SpeakerGridItem(card.speaker, card.speakerSelected)

    @Composable
    override fun SectionHeader(conference: String, text: String, icon: ImageVector) =
        ConfettiHeader(icon = icon, text = text)

    override fun hasPromo(placement: String, conference: String) = false

    @Composable
    override fun Promo(placement: String, conference: String, modifier: Modifier) {}
}

/**
 * Supplies the [ConferenceComponents] bound in Koin to [content]. Each platform's root calls the app
 * through this, so which implementation draws is a DI choice, not the root's.
 */
@Composable
fun WithConferenceComponents(
    components: ConferenceComponents = koinInject(),
    content: @Composable context(ConferenceComponents) () -> Unit,
) {
    content(components)
}
