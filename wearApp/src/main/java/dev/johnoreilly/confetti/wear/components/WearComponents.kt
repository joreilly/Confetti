package dev.johnoreilly.confetti.wear.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.datetime.LocalDateTime

/**
 * What a conference can restyle on the watch: the session cards and section headers of its lists,
 * and the tops of its session and speaker pages. The Wear counterpart of shared's
 * `ConferenceComponents`, kept to what a watch draws.
 *
 * Each slot is handed Confetti's own composable as [native]: an implementation that has nothing
 * for a conference (the default, [NativeWearComponents]) just draws it. The screens don't change
 * with the implementation; the sduiDebug build binds Livery's (see its WearSduiModule).
 */
interface WearComponents {
    /** One session in a list (sessions, bookmarks, home). */
    @Composable
    fun SessionCard(
        conference: String,
        session: SessionDetails,
        currentTime: LocalDateTime?,
        isBookmarked: Boolean,
        onOpen: () -> Unit,
        /** Adds or removes the bookmark; null where the list has none. */
        onToggleBookmark: (() -> Unit)?,
        modifier: Modifier,
        native: @Composable () -> Unit,
    )

    /** A list's section header: a time, "Bookmarked sessions", "Conference days". */
    @Composable
    fun SectionHeader(conference: String, text: String, modifier: Modifier, native: @Composable () -> Unit)

    /** The top of a session's page: its title and time. */
    @Composable
    fun SessionHeader(conference: String, session: SessionDetails, modifier: Modifier, native: @Composable () -> Unit)

    /** The top of a speaker's page: their photo, name and tagline. */
    @Composable
    fun SpeakerHeader(conference: String, speaker: SpeakerDetails, modifier: Modifier, native: @Composable () -> Unit)
}

/** Confetti's own composables, everywhere. */
object NativeWearComponents : WearComponents {
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
    ) = native()

    @Composable
    override fun SectionHeader(conference: String, text: String, modifier: Modifier, native: @Composable () -> Unit) = native()

    @Composable
    override fun SessionHeader(conference: String, session: SessionDetails, modifier: Modifier, native: @Composable () -> Unit) = native()

    @Composable
    override fun SpeakerHeader(conference: String, speaker: SpeakerDetails, modifier: Modifier, native: @Composable () -> Unit) = native()
}

/** The watch's [WearComponents], provided at the app's root from Koin. */
val LocalWearComponents = staticCompositionLocalOf<WearComponents> { NativeWearComponents }

/** The conference the watch shows, provided at the app's root; null before one is chosen. */
val LocalWearConference = compositionLocalOf<String?> { null }
