package dev.johnoreilly.confetti.wear.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.wear.compose.material3.Typography

/**
 * Confetti's ship typography — the **Expressive** stack from
 * [design/STYLE_GUIDE.md](../../../../../../../../design/STYLE_GUIDE.md§3):
 * Roboto Flex for display/title and numerals (variable axes respond to
 * motion, the M3 Expressive poster font), Inter for body and label (hinted
 * for legibility at small sizes on round displays).
 *
 * Both [FontFamily] values — [RobotoFlexFamily] and [InterFamily] — are
 * downloadable `GoogleFont`s (see [FontFamilies.kt][FontFamilies]). Robolectric
 * previews resolve them through the `ee.schimke.composeai.preview` plugin's
 * committed font cache; production builds fetch via Google Play services.
 *
 * The Wear scale (sizes, weights, arc + numeral styles) is preserved as-is.
 */
val ExpressiveTypography: Typography = Typography().withFamilies(
    display = RobotoFlexFamily, title = RobotoFlexFamily,
    body = InterFamily, label = InterFamily,
)

/**
 * Google Sans Flex across the board — the family Google released under OFL
 * in late 2025. Used by the DevFest conference theme via [ConferenceTheme].
 * A single-family pairing matches the way Google uses it on
 * developers.google.com and the DevFest site.
 */
val GoogleSansFlexTypography: Typography = Typography().withFamilies(
    display = GoogleSansFlexFamily, title = GoogleSansFlexFamily,
    body = GoogleSansFlexFamily, label = GoogleSansFlexFamily,
)

/**
 * KotlinConf's typography: JetBrains Mono for display + title + numerals —
 * the company's own OFL-licensed monospace face — paired with Inter for body
 * and label. Mono titles give the conference name a terminal/IDE feel that
 * matches Kotlin's developer identity; Inter keeps session-card content
 * readable at 12–14 dp where a full monospace body would be exhausting.
 */
val KotlinConfTypography: Typography = Typography().withFamilies(
    display = JetBrainsMonoFamily, title = JetBrainsMonoFamily,
    body = InterFamily, label = InterFamily,
)

private fun Typography.withFamilies(
    display: FontFamily,
    title: FontFamily,
    body: FontFamily,
    label: FontFamily,
): Typography = Typography(
    arcLarge = arcLarge,
    arcMedium = arcMedium,
    arcSmall = arcSmall,
    displayLarge = displayLarge.copy(fontFamily = display),
    displayMedium = displayMedium.copy(fontFamily = display),
    displaySmall = displaySmall.copy(fontFamily = display),
    titleLarge = titleLarge.copy(fontFamily = title),
    titleMedium = titleMedium.copy(fontFamily = title),
    titleSmall = titleSmall.copy(fontFamily = title),
    labelLarge = labelLarge.copy(fontFamily = label),
    labelMedium = labelMedium.copy(fontFamily = label),
    labelSmall = labelSmall.copy(fontFamily = label),
    bodyLarge = bodyLarge.copy(fontFamily = body),
    bodyMedium = bodyMedium.copy(fontFamily = body),
    bodySmall = bodySmall.copy(fontFamily = body),
    bodyExtraSmall = bodyExtraSmall.copy(fontFamily = body),
    numeralExtraLarge = numeralExtraLarge.copy(fontFamily = display),
    numeralLarge = numeralLarge.copy(fontFamily = display),
    numeralMedium = numeralMedium.copy(fontFamily = display),
    numeralSmall = numeralSmall.copy(fontFamily = display),
    numeralExtraSmall = numeralExtraSmall.copy(fontFamily = display),
)
