package dev.johnoreilly.confetti.wear.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * **Theme foundations** for the Wear catalog — one sticker per theme Confetti can actually run in.
 *
 * The Wear app doesn't have a single theme: [ConfettiApp] resolves a *per-conference* identity
 * (seed colour + optional brand typography) from [conferenceThemeFor] and feeds it to
 * [ConfettiTheme], so KotlinConf, AndroidMakers, Droidcon and DevFest each render the same
 * components in their own palette (and, for KotlinConf / DevFest, their own type family). Only the
 * stock scheme was published before, so the Themes tab claimed Confetti had one look when it has
 * five.
 *
 * Each preview goes through [ConfettiConferenceTheme] — the same call the app makes — and shows the
 * resolved `MaterialTheme.colorScheme` roles plus the type ramp, so a swatch here is by
 * construction the colour the app paints. The five ids below are prefix-matched by
 * [conferenceThemeFor], so they stay correct across rolling editions.
 *
 * Kept in the **main** source set so the `ee.schimke.composeai.preview` plugin discovers them for
 * the published design catalog; registered in `catalog.spec.json` under the Themes section. The
 * `@ThemeCatalog` providers in [ConfettiThemeCatalogs] expose the same five themes to
 * `compose-preview serve`, so any preview can be re-rendered live under any of them.
 */

/** Watch-sized swatch: the role colour with its on-role glyph, token name beneath. */
@Composable
private fun RowScope.RoleSwatch(label: String, token: String, color: Color, onColor: Color) {
    Column(Modifier.weight(1f)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, color = onColor, style = MaterialTheme.typography.labelSmall)
        }
        Text(
            token,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** One step of the surface ramp — outlined so adjacent near-black darks stay distinguishable. */
@Composable
private fun RowScope.SurfaceBand(label: String, color: Color) {
    Box(
        Modifier
            .weight(1f)
            .height(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        )
    }
}

/**
 * The foundation sheet itself: colour roles, the surface ramp and the type ramp, all read from the
 * enclosing [MaterialTheme] — i.e. whatever theme the caller wrapped this in.
 */
@Composable
private fun WearThemeFoundation(title: String, signature: String) {
    val cs = MaterialTheme.colorScheme
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(cs.background)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = cs.primary, maxLines = 1)
            Text(
                signature,
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
                maxLines = 2,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RoleSwatch("P", "primary", cs.primary, cs.onPrimary)
            RoleSwatch("PC", "primCont", cs.primaryContainer, cs.onPrimaryContainer)
            RoleSwatch("S", "secondary", cs.secondary, cs.onSecondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RoleSwatch("T", "tertiary", cs.tertiary, cs.onTertiary)
            RoleSwatch("TC", "tertCont", cs.tertiaryContainer, cs.onTertiaryContainer)
            RoleSwatch("E", "error", cs.error, cs.onError)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SurfaceBand("bg", cs.background)
            SurfaceBand("low", cs.surfaceContainerLow)
            SurfaceBand("con", cs.surfaceContainer)
            SurfaceBand("high", cs.surfaceContainerHigh)
        }
        Column {
            Text("Confetti", style = MaterialTheme.typography.displaySmall, color = cs.onBackground, maxLines = 1)
            Text("Title Medium", style = MaterialTheme.typography.titleMedium, color = cs.onBackground, maxLines = 1)
            Text(
                "Body — quick brown fox",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
                maxLines = 1,
            )
            Text("LABEL MEDIUM", style = MaterialTheme.typography.labelMedium, color = cs.primary, maxLines = 1)
        }
    }
}

@Preview(widthDp = 227)
@Composable
fun ThemeFoundationConfettiPreview() {
    // No curated conference → the stock Confetti Wear theme: Wear M3 defaults + ExpressiveTypography.
    ConfettiConferenceTheme(conferenceId = null) {
        WearThemeFoundation("Confetti", "Default · Roboto Flex / Inter")
    }
}

@Preview(widthDp = 227)
@Composable
fun ThemeFoundationKotlinConfPreview() {
    ConfettiConferenceTheme(conferenceId = "kotlinconf2025") {
        WearThemeFoundation("KotlinConf", "JetBrains purple · JetBrains Mono titles")
    }
}

@Preview(widthDp = 227)
@Composable
fun ThemeFoundationAndroidMakersPreview() {
    ConfettiConferenceTheme(conferenceId = "androidmakers2025") {
        WearThemeFoundation("AndroidMakers", "Parisian ochre · Roboto Flex / Inter")
    }
}

@Preview(widthDp = 227)
@Composable
fun ThemeFoundationDroidconPreview() {
    ConfettiConferenceTheme(conferenceId = "droidconlondon2025") {
        WearThemeFoundation("Droidcon", "Droidcon green · Roboto Flex / Inter")
    }
}

@Preview(widthDp = 227)
@Composable
fun ThemeFoundationDevFestPreview() {
    ConfettiConferenceTheme(conferenceId = "devfest2025") {
        WearThemeFoundation("DevFest", "Google blue · Google Sans Flex")
    }
}
