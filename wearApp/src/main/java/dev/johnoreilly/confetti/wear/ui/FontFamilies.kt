package dev.johnoreilly.confetti.wear.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import dev.johnoreilly.confetti.R

/**
 * Font sources for the Expressive typography: downloadable fonts via Google
 * Play services' Google Fonts provider. Keeps the APK small (no bundled
 * TTFs). Offline devices substitute the Compose font-loader's system
 * fallback until the download completes.
 *
 * Under Robolectric previews the same code path works: the
 * `ee.schimke.composeai.preview` plugin installs a shadow of
 * `FontsContractCompat.requestFont` that resolves each `GoogleFont` against
 * a local cache under `.compose-preview-history/fonts/`, fetched from the
 * Google Fonts CSS API on first use and committed to the repo so subsequent
 * builds are offline.
 */
private val GoogleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private fun downloadable(name: String): FontFamily {
    val gf = GoogleFont(name)
    return FontFamily(
        Font(googleFont = gf, fontProvider = GoogleFontsProvider, weight = FontWeight.Normal),
        Font(googleFont = gf, fontProvider = GoogleFontsProvider, weight = FontWeight.Medium),
        Font(googleFont = gf, fontProvider = GoogleFontsProvider, weight = FontWeight.SemiBold),
        Font(googleFont = gf, fontProvider = GoogleFontsProvider, weight = FontWeight.Bold),
    )
}

internal val RobotoFlexFamily: FontFamily = downloadable("Roboto Flex")
internal val InterFamily: FontFamily = downloadable("Inter")
/** Google Sans Flex — the brand family released by Google under OFL in late
 *  2025. Used by the DevFest conference theme; see [ConferenceTheme]. */
internal val GoogleSansFlexFamily: FontFamily = downloadable("Google Sans Flex")
/** JetBrains Mono — JetBrains' OFL-licensed monospaced typeface. Used by
 *  the KotlinConf conference theme for display/title roles. */
internal val JetBrainsMonoFamily: FontFamily = downloadable("JetBrains Mono")
