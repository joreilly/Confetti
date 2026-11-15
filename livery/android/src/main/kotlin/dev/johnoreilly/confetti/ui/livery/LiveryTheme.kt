package dev.johnoreilly.confetti.ui.livery

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
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
 *   Its headings take the face its designs ship (Poppins, Space Mono), dynamic colour or not.
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
    val font = catalog.rememberBrandFont()
    // The light pack's primary is the brand's hue in both modes: the scheme picks its tones.
    native.ConferenceTheme(conference, light?.color(ThemeRoles.PRIMARY)?.toSeed() ?: themeColor) {
        val scheme = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography
        // The user's wallpaper colours win; the brand's face still applies.
        val pack = when {
            LocalDynamicColorInUse.current -> null
            scheme.surface.luminance() < 0.5f -> dark
            else -> light
        }
        val branded = remember(scheme, pack) { pack?.let(scheme::withThemePack) ?: scheme }
        val type = remember(typography, font) { font?.let(typography::withHeadingFamily) ?: typography }
        MaterialTheme(colorScheme = branded, typography = type, shapes = MaterialTheme.shapes, content = content)
    }
}

/**
 * [this] with [family] for its headings (display, headline and title): the brand's face where it
 * speaks loudest. Body and label text keep the platform's, for reading (droidcon's face is
 * monospaced). Only the family: sizes, weights and line heights stay Confetti's, so no layout moves.
 */
internal fun Typography.withHeadingFamily(family: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = family),
    displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family),
    headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family),
    headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family),
    titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family),
)

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
