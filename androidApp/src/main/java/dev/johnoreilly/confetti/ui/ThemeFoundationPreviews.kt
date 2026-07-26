package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * **Theme foundations** for the phone catalog — one sticker per theme the mobile app can actually
 * run in, each rendered light + dark by [CatalogModes].
 *
 * The mobile app has two independent theming mechanisms and the catalog previously published
 * neither of them honestly — it pinned a single brand scheme with dynamic colour switched off, so
 * the Themes tab implied Confetti had one look:
 *
 *  - [ConfettiTheme] picks between three colour schemes — the Confetti **brand** purple, the
 *    **Android** green, and **dynamic** (Material You, wallpaper-derived on Android 12+) — with
 *    precedence dynamic > Android > brand.
 *  - [ConferenceMaterialTheme] is what the real app root
 *    ([dev.johnoreilly.confetti.ui.App]) actually installs: a scheme generated from the active
 *    **conference's seed colour**, so the app re-tints per conference the way the Wear app does.
 *
 * Every preview below goes through the production theme function rather than re-declaring a
 * scheme, so a swatch here is by construction the colour the app paints. Kept in the **main**
 * source set for the `ee.schimke.composeai.preview` plugin, registered in
 * `catalog.mobile.spec.json`, and mirrored by the `@ThemeCatalog` providers in
 * [ConfettiThemeCatalogs] so `compose-preview serve` can re-render any preview live under any of
 * them.
 */

/** A colour-role swatch: the role colour carrying its on-role label, token name beneath. */
@Composable
private fun RowScope.RoleSwatch(label: String, token: String, color: Color, onColor: Color) {
    Column(Modifier.weight(1f)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, color = onColor, style = MaterialTheme.typography.labelSmall)
        }
        Text(
            token,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/** One step of the surface ramp — outlined so adjacent near-identical tones stay readable. */
@Composable
private fun RowScope.SurfaceBand(label: String, color: Color) {
    Box(
        Modifier
            .weight(1f)
            .height(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/**
 * The foundation sheet: colour roles, surface ramp and type ramp, all read from the enclosing
 * [MaterialTheme] — i.e. whatever theme the caller wrapped this in.
 */
@Composable
private fun ThemeFoundation(title: String, tagline: String) {
    val cs = MaterialTheme.colorScheme
    Surface(color = cs.background) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, color = cs.primary)
                Text(tagline, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleSwatch("P", "primary", cs.primary, cs.onPrimary)
                RoleSwatch("PC", "primCont", cs.primaryContainer, cs.onPrimaryContainer)
                RoleSwatch("S", "secondary", cs.secondary, cs.onSecondary)
                RoleSwatch("T", "tertiary", cs.tertiary, cs.onTertiary)
                RoleSwatch("E", "error", cs.error, cs.onError)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SurfaceBand("0", cs.surface)
                SurfaceBand("1", cs.surfaceContainerLowest)
                SurfaceBand("2", cs.surfaceContainerLow)
                SurfaceBand("3", cs.surfaceContainer)
                SurfaceBand("4", cs.surfaceContainerHigh)
                SurfaceBand("5", cs.surfaceContainerHighest)
            }
            Column {
                Text("Display", style = MaterialTheme.typography.displaySmall, color = cs.onBackground)
                Text("Headline", style = MaterialTheme.typography.headlineSmall, color = cs.onBackground)
                Text("Title", style = MaterialTheme.typography.titleMedium, color = cs.onBackground)
                Text(
                    "Body — the quick brown fox jumps over the lazy dog",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                )
                Text("LABEL", style = MaterialTheme.typography.labelLarge, color = cs.primary)
            }
        }
    }
}

/**
 * Seed colour used for the conference-themed specimen — KotlinConf's JetBrains purple, the same
 * `0xFF7F52FF` the Wear catalog curates for that conference, so the two sheets line up. The live
 * app takes this string from the backend's `conference.themeColor`; the `0x` prefix is part of the
 * wire format ([ConferenceMaterialTheme] parses with `HexFormat { number.prefix = "0x" }`, and a
 * string without it silently falls back to the default green).
 */
private const val CONFERENCE_SEED = "0xFF7F52FF"

@CatalogModes
@Composable
fun ThemeFoundationDefaultPreview() {
    // Dynamic explicitly off → the Confetti brand scheme, whatever the render host's wallpaper is.
    ConfettiTheme(disableDynamicTheming = true) {
        ThemeFoundation("Confetti brand", "Purple / orange / blue · the default scheme")
    }
}

@CatalogModes
@Composable
fun ThemeFoundationAndroidPreview() {
    ConfettiTheme(androidTheme = true) {
        ThemeFoundation("Android", "Green / teal · the Android-branded scheme")
    }
}

@CatalogModes
@Composable
fun ThemeFoundationDynamicPreview() {
    // Material You: `colorScheme()` resolves `dynamic{Light,Dark}ColorScheme(context)` on API 31+,
    // and falls back to the brand scheme below that — so on an older render host this sticker is
    // the brand scheme, which is exactly what the app would show there too.
    ConfettiTheme(disableDynamicTheming = false) {
        ThemeFoundation("Dynamic (Material You)", "Wallpaper-derived on Android 12+")
    }
}

@CatalogModes
@Composable
fun ThemeFoundationConferenceSeedPreview() {
    // The real app-root theme: a scheme generated from the active conference's seed colour.
    ConferenceMaterialTheme(seedColorString = CONFERENCE_SEED) {
        ThemeFoundation("Conference seed", "Generated from conference.themeColor · #7F52FF")
    }
}
