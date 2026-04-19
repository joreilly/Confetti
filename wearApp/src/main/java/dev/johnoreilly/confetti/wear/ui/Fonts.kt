package dev.johnoreilly.confetti.wear.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.wear.compose.material3.Typography
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.wear.proto.Typography as ProtoTypography

// Single downloadable-font provider; cert array is shipped in res/values/font_certs.xml.
private val GoogleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private fun googleFontFamily(name: String): FontFamily {
    val gf = GoogleFont(name)
    // FontFamily's fallback chain kicks in automatically when the download
    // hasn't arrived yet — offline renders substitute a legible system face.
    return FontFamily(
        Font(googleFont = gf, fontProvider = GoogleFontsProvider, weight = FontWeight.Normal),
        Font(googleFont = gf, fontProvider = GoogleFontsProvider, weight = FontWeight.Medium),
        Font(googleFont = gf, fontProvider = GoogleFontsProvider, weight = FontWeight.SemiBold),
        Font(googleFont = gf, fontProvider = GoogleFontsProvider, weight = FontWeight.Bold),
    )
}

// Variable-axis recommendation from WEAR_UI.md §4: Roboto Flex is M3 Expressive's
// poster font for motion-aware UI. Pair it with Inter for body legibility at tiny
// sizes — Inter ships with hinting tuned for small round displays.
private val RobotoFlex = googleFontFamily("Roboto Flex")
private val Inter = googleFontFamily("Inter")

// Editorial serif. Newsreader's contrast and terminals give conference titles a
// magazine weight. Public Sans keeps body copy neutral so serifs don't compete.
private val Newsreader = googleFontFamily("Newsreader")
private val PublicSans = googleFontFamily("Public Sans")

// Modern tech display. Space Grotesk's wide counters and slightly irregular
// geometry read "dev conference branding" (Droidcon, KotlinConf adjacent).
private val SpaceGrotesk = googleFontFamily("Space Grotesk")

/** User-selectable typography family. Mirrors the proto enum. */
enum class TypographyChoice(val label: String) {
    System("System"),
    Expressive("Expressive"),
    Editorial("Editorial"),
    Confident("Confident");

    fun toProto(): ProtoTypography = when (this) {
        System -> ProtoTypography.TYPOGRAPHY_SYSTEM
        Expressive -> ProtoTypography.TYPOGRAPHY_EXPRESSIVE
        Editorial -> ProtoTypography.TYPOGRAPHY_EDITORIAL
        Confident -> ProtoTypography.TYPOGRAPHY_CONFIDENT
    }

    fun next(): TypographyChoice = entries[(ordinal + 1) % entries.size]

    companion object {
        /** Map back from proto; UNSPECIFIED and unknown both fall back to System. */
        fun fromProto(proto: ProtoTypography?): TypographyChoice = when (proto) {
            ProtoTypography.TYPOGRAPHY_EXPRESSIVE -> Expressive
            ProtoTypography.TYPOGRAPHY_EDITORIAL -> Editorial
            ProtoTypography.TYPOGRAPHY_CONFIDENT -> Confident
            else -> System
        }
    }
}

/**
 * Build a Wear Material 3 [Typography] for the given [choice]. System returns
 * the Wear defaults (Roboto-based, no downloadable fetch). The other three
 * replace the default families role-by-role, leaving sizes untouched so the
 * Wear scale stays intact. Arc + numeral styles are preserved as-is — they
 * drive the curved time text and tile numerals, not app content.
 */
fun typographyFor(choice: TypographyChoice): Typography {
    val base = Typography()
    return when (choice) {
        TypographyChoice.System -> base
        TypographyChoice.Expressive -> base.withFamilies(
            display = RobotoFlex, title = RobotoFlex, body = Inter, label = Inter,
        )
        TypographyChoice.Editorial -> base.withFamilies(
            display = Newsreader, title = Newsreader, body = PublicSans, label = PublicSans,
        )
        TypographyChoice.Confident -> base.withFamilies(
            display = SpaceGrotesk, title = SpaceGrotesk, body = Inter, label = Inter,
        )
    }
}

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
