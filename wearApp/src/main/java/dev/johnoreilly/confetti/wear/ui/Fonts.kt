package dev.johnoreilly.confetti.wear.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.wear.compose.material3.Typography
import dev.johnoreilly.confetti.wear.proto.Typography as ProtoTypography

/**
 * User-selectable typography family. Mirrors the proto enum. The actual
 * [FontFamily] instances that back each option live in `FontFamilies.kt` —
 * **debug** builds load bundled TTFs from `res/font/`, so Robolectric
 * previews show the selected typography faithfully; **release** builds use
 * downloadable GoogleFonts to keep the APK small.
 */
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
 * the Wear defaults (no font resource loaded). The other three replace the
 * default families role-by-role, leaving sizes untouched so the Wear scale
 * stays intact. Arc + numeral styles are preserved as-is — they drive the
 * curved time text and tile numerals, not app content.
 */
fun typographyFor(choice: TypographyChoice): Typography {
    val base = Typography()
    return when (choice) {
        TypographyChoice.System -> base
        TypographyChoice.Expressive -> base.withFamilies(
            display = RobotoFlexFamily, title = RobotoFlexFamily,
            body = InterFamily, label = InterFamily,
        )
        TypographyChoice.Editorial -> base.withFamilies(
            display = NewsreaderFamily, title = NewsreaderFamily,
            body = PublicSansFamily, label = PublicSansFamily,
        )
        TypographyChoice.Confident -> base.withFamilies(
            display = SpaceGroteskFamily, title = SpaceGroteskFamily,
            body = InterFamily, label = InterFamily,
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
