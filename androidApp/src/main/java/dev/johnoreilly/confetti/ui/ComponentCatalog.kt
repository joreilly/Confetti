package dev.johnoreilly.confetti.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.preview.breakSession
import dev.johnoreilly.confetti.preview.johnOreillySpeaker
import dev.johnoreilly.confetti.preview.lightningSession
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.preview.sessionsSuccessState
import dev.johnoreilly.confetti.ui.bookmarks.Bookmark
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.icons.AccessTime
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.sessions.SessionDetailViewShared
import dev.johnoreilly.confetti.ui.sessions.SessionItemView
import dev.johnoreilly.confetti.ui.sessions.SessionListView
import dev.johnoreilly.confetti.ui.speakers.SpeakerDetailsView
import dev.johnoreilly.confetti.ui.speakers.SpeakerItemView

/**
 * Single-component "sticker sheet" catalog for the **phone** app — the mobile counterpart of
 * `dev.johnoreilly.confetti.wear.ui.ComponentCatalog`. Each `@Preview` wraps exactly one real shared
 * component (or a theme specimen) in the real [ConfettiTheme] fed by `dev.johnoreilly.confetti.preview`
 * mock data, rendered from the **main** source set so the `ee.schimke.composeai.preview` plugin
 * discovers them for the published design catalog (it discovers `@Preview`s in the rendered module's
 * main classes only — the shared module's own CMP previews are invisible to a `:androidApp` render).
 *
 * The theme is pinned with `disableDynamicTheming = true` so the render always shows Confetti's own
 * brand scheme rather than the render host's Material-You dynamic colours — the catalog is a brand
 * reference. `@CatalogModes` captures every sticker in light + dark; `@CatalogScreen` frames the
 * full-screen entries on a phone.
 */

/** Confetti's brand theme with dynamic (Material You) theming disabled, so the catalog is deterministic. */
@Composable
private fun CatalogTheme(content: @Composable () -> Unit) {
    ConfettiTheme(disableDynamicTheming = true, content = content)
}

/** Component sticker multipreview — light + dark, phone width, height wraps to the component. */
@Preview(name = "Light", widthDp = 411, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 411, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class CatalogModes

/**
 * Session-row multipreview — a bounded height, because [SessionItemView]'s root is `fillMaxSize`, so
 * without a fixed height it would fill the whole 800dp sandbox and leave the sticker mostly empty.
 */
@Preview(name = "Light", widthDp = 411, heightDp = 190, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 411, heightDp = 190, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class CatalogRow

/** Full-screen multipreview — light + dark on a phone frame. */
@Preview(name = "Light", widthDp = 411, heightDp = 914, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 411, heightDp = 914, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class CatalogScreen

// ---------------------------------------------------------------------------
// Components.
// ---------------------------------------------------------------------------

@CatalogRow
@Composable
fun SessionItemPopulatedPreview() {
    CatalogTheme {
        SessionItemView(
            session = sessionDetails,
            sessionSelected = {},
            isBookmarked = true,
            addBookmark = {},
            removeBookmark = {},
            isLoggedIn = true,
        )
    }
}

@CatalogRow
@Composable
fun SessionItemLightningPreview() {
    CatalogTheme {
        SessionItemView(
            session = lightningSession,
            sessionSelected = {},
            isBookmarked = false,
            addBookmark = {},
            removeBookmark = {},
            isLoggedIn = false,
        )
    }
}

@Preview(name = "Light", widthDp = 411, heightDp = 110, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 411, heightDp = 110, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SessionItemBreakPreview() {
    CatalogTheme {
        SessionItemView(
            session = breakSession,
            sessionSelected = {},
            isBookmarked = false,
            addBookmark = {},
            removeBookmark = {},
            isLoggedIn = false,
        )
    }
}

@CatalogModes
@Composable
fun BookmarkAddPreview() {
    CatalogTheme { Bookmark(isBookmarked = false, onBookmarkChange = {}) }
}

@CatalogModes
@Composable
fun BookmarkOnPreview() {
    CatalogTheme { Bookmark(isBookmarked = true, onBookmarkChange = {}) }
}

@CatalogModes
@Composable
fun SpeakerItemPreview() {
    CatalogTheme { SpeakerItemView(speaker = johnOreillySpeaker, navigateToSpeaker = {}) }
}

@CatalogModes
@Composable
fun ConfettiHeaderPreview() {
    CatalogTheme { ConfettiHeader(text = "14:00", icon = ConfettiIcons.AccessTime) }
}

// ---------------------------------------------------------------------------
// Theme specimens — the Confetti phone type ramp and colour-scheme swatches read straight from the
// real [MaterialTheme], mirroring the Wear catalog's specimens on Confetti's own brand theme.
// ---------------------------------------------------------------------------

@CatalogModes
@Composable
fun TypographySpecimenPreview() {
    CatalogTheme {
        Column {
            Text("Display Small", style = MaterialTheme.typography.displaySmall)
            Text("Title Large", style = MaterialTheme.typography.titleLarge)
            Text("Body Large", style = MaterialTheme.typography.bodyLarge)
            Text("Label Small", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@CatalogModes
@Composable
fun ColorSchemeSpecimenPreview() {
    CatalogTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary))
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer))
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondary))
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.tertiary))
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.surfaceVariant))
        }
    }
}

// ---------------------------------------------------------------------------
// Screens — full-screen entry points built from the real shared screens + mock state.
// ---------------------------------------------------------------------------

@CatalogScreen
@Composable
fun ScheduleScreenPreview() {
    CatalogTheme {
        SessionListView(
            uiState = sessionsSuccessState,
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {},
            onRefresh = {},
            onNavigateToSignIn = {},
            isLoggedIn = false,
        )
    }
}

@CatalogScreen
@Composable
fun SessionDetailsScreenPreview() {
    CatalogTheme {
        SessionDetailViewShared(
            conference = "kotlinconf2023",
            session = sessionDetails,
            onSpeakerClick = {},
            onSocialLinkClicked = {},
        )
    }
}

@CatalogScreen
@Composable
fun SpeakerDetailsScreenPreview() {
    CatalogTheme {
        SpeakerDetailsView(
            conference = "kotlinconf2023",
            speaker = johnOreillySpeaker,
            navigateToSession = {},
            popBack = {},
            onSocialLinkClicked = {},
        )
    }
}
