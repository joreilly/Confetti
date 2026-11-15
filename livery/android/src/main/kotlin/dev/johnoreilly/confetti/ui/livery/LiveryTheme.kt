package dev.johnoreilly.confetti.ui.livery

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import dev.johnoreilly.confetti.livery.ConferenceSeries
import dev.johnoreilly.confetti.ui.LocalDynamicColorInUse
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import ee.schimke.livery.client.Bindings
import ee.schimke.livery.protocol.NamedValue
import ee.schimke.livery.protocol.ThemeRoles

/*
 * A conference's colours for the whole of its screens, not only its slots: Confetti's own
 * composables (top bars, lists, the session and speaker pages) and the Livery designs drawn among
 * them take the same colours, from one source per conference.
 *
 * - The user's dynamic (wallpaper) colours, when they have them on: both sides take those, as
 *   before.
 * - A conference with a catalog of its own (droidcon, KotlinConf): its theme pack. The pack seeds
 *   the Material scheme in place of the server's `themeColor`, so every container and surface is
 *   derived in the brand's hue, then the roles the pack sets with their pairing are laid over it.
 * - Any other conference (the base catalog): its own `themeColor`, as Confetti draws it, and the
 *   base designs take that scheme instead of Confetti's purple, so they match the screen around
 *   them.
 */

/**
 * Around everything shown for [conference]: [native]'s theme in the colours above. [catalog] is
 * the conference's ([LiveryStyling.catalog]), null where there's none.
 */
@Composable
internal fun LiveryConferenceTheme(
    catalog: LiveryStyling.SeriesCatalog?,
    native: ConferenceComponents,
    conference: String,
    themeColor: String?,
    content: @Composable () -> Unit,
) {
    if (catalog == null || catalog.series == ConferenceSeries.BASE) {
        return native.ConferenceTheme(conference, themeColor) {
            CompositionLocalProvider(LocalSlotsFollowMaterial provides true, content = content)
        }
    }
    val light = catalog.rememberThemePack(dark = false)
    val dark = catalog.rememberThemePack(dark = true)
    // The light pack's primary is the brand's hue in both modes: the scheme picks its tones.
    native.ConferenceTheme(conference, light?.color(ThemeRoles.PRIMARY)?.toSeed() ?: themeColor) {
        if (LocalDynamicColorInUse.current) return@ConferenceTheme content()
        val scheme = MaterialTheme.colorScheme
        val pack = if (scheme.surface.luminance() < 0.5f) dark else light
        val branded = remember(scheme, pack) { pack?.let(scheme::withThemePack) ?: scheme }
        MaterialTheme(colorScheme = branded, typography = MaterialTheme.typography, shapes = MaterialTheme.shapes, content = content)
    }
}

/**
 * [this] with the [pack]'s roles that come with their pairing: primary on onPrimary, and the text
 * colours on a surface. The inverse of Livery's `toLiveryTheme()`, which sends a card's colour
 * (`surfaceContainerHigh`) as `theme.surface`: so the pack's `surface` (Confetti's is a tint) is
 * that container here, and Material's page surfaces, derived from the same seed, stay. secondary and
 * tertiary are left out: a pack has no `onSecondary` or `onTertiary` to keep their content legible.
 */
internal fun ColorScheme.withThemePack(pack: Bindings): ColorScheme = copy(
    primary = pack.color(ThemeRoles.PRIMARY) ?: primary,
    onPrimary = pack.color(ThemeRoles.ON_PRIMARY) ?: onPrimary,
    surfaceContainerHigh = pack.color(ThemeRoles.SURFACE) ?: surfaceContainerHigh,
    onSurface = pack.color(ThemeRoles.ON_SURFACE) ?: onSurface,
    onBackground = pack.color(ThemeRoles.ON_SURFACE) ?: onBackground,
    onSurfaceVariant = pack.color(ThemeRoles.ON_SURFACE_VARIANT) ?: onSurfaceVariant,
)

internal fun Bindings.color(role: String): Color? = (values[role] as? NamedValue.Color)?.let { Color(it.argb) }

/** As `ConferenceMaterialTheme` reads a seed: `0xAARRGGBB`. */
internal fun Color.toSeed(): String = "0x" + toArgb().toUInt().toString(16).uppercase().padStart(8, '0')
