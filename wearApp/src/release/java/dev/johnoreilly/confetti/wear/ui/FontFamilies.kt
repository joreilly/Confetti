package dev.johnoreilly.confetti.wear.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import dev.johnoreilly.confetti.R

/**
 * Release-variant font sources for the Expressive typography: downloadable
 * fonts via Google Play services' Google Fonts provider. Keeps the APK small
 * (no bundled TTFs). Offline devices substitute the Compose font-loader's
 * system fallback until the download completes.
 *
 * The debug variant bundles the actual variable TTFs so Robolectric
 * previews render the real fonts; see
 * `src/debug/java/dev/johnoreilly/confetti/wear/ui/FontFamilies.kt`.
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
