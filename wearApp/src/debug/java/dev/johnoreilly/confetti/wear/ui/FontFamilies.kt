@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package dev.johnoreilly.confetti.wear.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import dev.johnoreilly.confetti.R

/**
 * Debug-variant font sources for the Expressive typography: bundled variable
 * TTFs in `res/font/`. They load synchronously — no network round-trip — so
 * Robolectric previews and offline devices both show Roboto Flex + Inter
 * faithfully. The four declared weights all point at the same variable font
 * resource and interpolate via `FontVariation.weight(...)`.
 *
 * The release variant swaps these for a downloadable GoogleFonts provider;
 * see `src/release/java/dev/johnoreilly/confetti/wear/ui/FontFamilies.kt`.
 */
private fun variable(resourceId: Int): FontFamily = FontFamily(
    Font(resourceId, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(resourceId, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(resourceId, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(resourceId, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

internal val RobotoFlexFamily: FontFamily = variable(R.font.roboto_flex_variable)
internal val InterFamily: FontFamily = variable(R.font.inter_variable)
/** Google Sans Flex — the brand family released by Google under OFL in late
 *  2025. Used by the DevFest conference theme; see [ConferenceTheme]. */
internal val GoogleSansFlexFamily: FontFamily = variable(R.font.google_sans_flex_variable)
