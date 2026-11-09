package dev.johnoreilly.confetti.livery

import androidx.compose.ui.graphics.Color

/**
 * A conference's look as data. Every colour here is the *preview* (initial) value of a named
 * value in the document, so a theme pack (catalog/themes/<series>.json) can change it later
 * without re-capturing: that's the knob an admin's agent turns first.
 */
data class ConferenceBrand(
    /** The `series` tag on every design, which the app queries by. */
    val series: String,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val tertiary: Color,
    val surface: Color,
    val onSurface: Color,
    val muted: Color,
    /**
     * The typeface every label is set in: a family in the series catalog's `fonts/fonts.json`
     * (shipped with the templates, so it draws the same on every device) or `system:<family>`.
     * Null: the platform's default.
     */
    val fontFamily: String?,
)

/**
 * Confetti's own look, for every conference without a catalog of its own: the app's Material
 * colours (the theme packs set them per mode), the platform's default typeface.
 */
val Confetti = ConferenceBrand(
    series = ConferenceSeries.BASE,
    primary = Color(0xFF8C4190), // Confetti purple (Purple40)
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFA23F16), // Orange40
    tertiary = Color(0xFF006781), // Blue40
    surface = Color(0xFFEDDEE8),
    onSurface = Color(0xFF201A1B),
    muted = Color(0xFF7F747C),
    fontFamily = null,
)

/** Every brand with a catalog; conference brands join in their own PRs. */
val Droidcon = ConferenceBrand(
    series = "droidcon",
    primary = Color(0xFF3DDC84), // Android green
    onPrimary = Color(0xFF0E1116),
    secondary = Color(0xFFB4F54A),
    tertiary = Color(0xFF4285F4),
    surface = Color(0xFF0E1116),
    onSurface = Color(0xFFF5F7FA),
    muted = Color(0xFF9AA4B2),
    // Terminal-flavoured, to go with the green-on-black.
    fontFamily = "Space Mono",
)

val AllBrands: List<ConferenceBrand> = listOf(Confetti, Droidcon)
