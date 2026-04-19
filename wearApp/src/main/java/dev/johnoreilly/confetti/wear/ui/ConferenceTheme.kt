package dev.johnoreilly.confetti.wear.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Event
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.wear.compose.material3.Typography

/**
 * A curated per-conference visual identity: a seed [Color] that drives the
 * Material 3 dynamic scheme, a recognisable [icon] to sit beside the
 * conference name on [HomeScreen], a short [signatureName] to cite in the
 * style guide, and an optional [typography] override when the conference has
 * a distinctive brand font (e.g. DevFest uses Google Sans Flex). See
 * [design/STYLE_GUIDE.md](../../../../../../../../design/STYLE_GUIDE.md§4)
 * for the editorial rationale behind each.
 */
data class ConferenceTheme(
    val seedColor: Color,
    val icon: ImageVector,
    val signatureName: String,
    val typography: Typography? = null,
)

/**
 * Look up the [ConferenceTheme] for a given conference id. Returns null for
 * conferences we haven't curated — those keep whatever `themeColor` the
 * backend provides and fall back to the default icon-less HomeScreen header.
 *
 * Ids are prefix-matched so rolling editions (kotlinconf2023, kotlinconf2024)
 * all pick up the same identity.
 */
fun conferenceThemeFor(id: String?): ConferenceTheme? {
    if (id.isNullOrBlank()) return null
    val normalised = id.lowercase()
    return when {
        // KotlinConf: JetBrains purple — the warm end of the red→purple
        // gradient the 2025/2026 site leans on. Code brackets icon nods to
        // the language; the gradient itself is too loud for a 24 dp chip.
        normalised.startsWith("kotlinconf") -> ConferenceTheme(
            seedColor = Color(0xFF7F52FF),
            icon = Icons.Filled.Code,
            signatureName = "KotlinConf purple",
        )
        // AndroidMakers: warm Parisian ochre, leaning into the venue imagery
        // the droidcon-run edition uses. Android robot icon is the one piece
        // of visual shorthand that works at 24 dp on a watch face.
        normalised.startsWith("androidmakers") -> ConferenceTheme(
            seedColor = Color(0xFFE59A4F),
            icon = Icons.Filled.Android,
            signatureName = "Parisian ochre",
        )
        // Droidcon: the green is the entire identity — used boldly at full
        // saturation on their site. We can afford to let primary carry most
        // of the weight here. The Adb glyph is a stylised droid bot.
        normalised.startsWith("droidcon") -> ConferenceTheme(
            seedColor = Color(0xFF00D775),
            icon = Icons.Filled.Adb,
            signatureName = "Droidcon green",
        )
        // DevFest: Google Blue anchors the identity; the celebration icon
        // matches the "festival" framing Google uses across GDG sites. This
        // is the only conference that also swaps typography — Google Sans
        // Flex is their brand family (OFL-licensed since late 2025), and
        // using it here locks the identity together.
        normalised.startsWith("devfest") -> ConferenceTheme(
            seedColor = Color(0xFF4285F4),
            icon = Icons.Filled.Celebration,
            signatureName = "Google blue + Google Sans Flex",
            typography = GoogleSansFlexTypography,
        )
        else -> null
    }
}

/** Fallback icon for conferences without a curated theme — used when we still
 *  want a consistent header shape. */
val GenericConferenceIcon: ImageVector = Icons.Filled.Event
