package dev.johnoreilly.confetti.livery

import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteContentScale
import androidx.compose.remote.creation.compose.layout.RemoteImage
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.border
import androidx.compose.remote.creation.compose.modifier.clickable
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.height
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.modifier.visibility
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/*
 * droidcon: dark, terminal-flavoured, square-ish corners, Android green accents, uppercase
 * monospace-like labels and a green rule down the left edge.
 */

/** droidcon's designs, captured into catalogs/droidcon. */
val DroidconDesigns: List<DesignDef> = designs(
    Droidcon, "default", "droidcon",
    header = { DroidconHeader() },
    session = { DroidconSession() },
    speaker = { with(ContractScope(SpeakerContract, Droidcon)) { DroidconSpeaker() } },
    promo = { DroidconPromo() },
    // Space Mono is wide: the tagline wraps, and the tickets button needs the extra rows.
    headerHeightDp = 250,
    // At the contracts' limits: a two-line tagline pushes the speaker's sessions out at 110dp,
    // and a three-line body the promo's CTA at 200dp.
    speakerHeightDp = 130,
    promoHeightDp = 250,
    sessionWidget = { DroidconSessionWidget() },
    sessionWear = { DroidconSessionWear() },
)

@Composable
fun DroidconHeader() {
    val C = ConferenceContract
    val c = brandColors(Droidcon)
    RemoteColumn(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(8.rdp))
            .background(c.surface)
            // A faint terminal glow: green and blue light drifting behind the copy.
            .meshGlows(listOf(
                Glow(c.primary.copy(alpha = 0.2f.rf), x = 320f, y = 30f, radius = 200f, periodSeconds = 32f),
                Glow(c.tertiary.copy(alpha = 0.16f.rf), x = 40f, y = 240f, radius = 190f, periodSeconds = 24f, phase = 3f),
            ))
            .border(1.rdp, c.primary, RemoteRoundedCornerShape(8.rdp))
            .padding(20.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
    ) {
        RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically, horizontalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
            Label("> ".rs + input(C.dates), c.primary, size = 13)
            LivePill(inputBool(C.live), c.primary, c.onPrimary, text = "HAPPENING NOW")
        }
        Label(input(C.name), c.onSurface, size = 30, weight = FontWeight.ExtraBold, maxLines = 2)
        // One line: with a two-line name in Space Mono, a second leaves the tickets button no room.
        Label(input(C.tagline), c.muted, size = 15, weight = FontWeight.Normal, maxLines = 1)
        Label("◉ ".rs + input(C.location), c.tertiary, size = 13, weight = FontWeight.SemiBold)
        // Only when the app has somewhere to send people: no URL, no button.
        val ticketsUrl = input(C.ticketsUrl)
        RemoteBox(
            modifier = RemoteModifier
                .visibility(ticketsUrl.isNotEmpty.visibleIf())
                .padding(top = 8.rdp)
                .clip(RemoteRoundedCornerShape(4.rdp))
                .background(c.primary)
                .clickable(emit(C.tickets, ticketsUrl))
                .padding(horizontal = 14.rdp, vertical = 6.rdp),
        ) { Label("GET TICKETS →".rs, c.onPrimary, size = 13) }
    }
}

@Composable
fun DroidconSession() {
    val C = SessionContract
    val c = brandColors(Droidcon)
    val bookmarked = inputBool(C.bookmarked)
    RemoteRow(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(6.rdp))
            // The green rule down the left edge: the row's own background, 4dp of it left showing.
            .background(c.primary)
            .padding(start = 4.rdp)
            .background(c.surface)
            .clickable(emit(C.open)),
    ) {
        RemoteColumn(
            modifier = RemoteModifier.weight(1f).padding(start = 14.rdp, top = 12.rdp, end = 8.rdp, bottom = 12.rdp),
            verticalArrangement = RemoteArrangement.spacedBy(4.rdp),
        ) {
            RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically, horizontalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
                Label(input(C.time), c.primary, size = 12)
                LivePill(inputBool(C.live), c.primary, c.onPrimary)
                Label("@ ".rs + input(C.room), c.muted, size = 12, weight = FontWeight.Medium, modifier = RemoteModifier.weight(1f))
            }
            Label(input(C.title), c.onSurface, size = 17, weight = FontWeight.Bold, maxLines = 2)
            Label(input(C.speakers), c.muted, size = 13, weight = FontWeight.Normal)
            Chip("#".rs + input(C.tag), c.tertiary.copy(alpha = 0.18f.rf), c.tertiary, corner = 4)
        }
        RemoteBox(
            modifier = RemoteModifier.padding(8.rdp).size(40.rdp, 40.rdp).clickable(emit(C.bookmark)),
            contentAlignment = RemoteAlignment.Center,
        ) {
            Label(bookmarked.select("★".rs, "☆".rs), bookmarked.select(c.secondary, c.muted), size = 22)
        }
    }
}

@Composable
context(_: ContractScope<SpeakerContract>)
fun DroidconSpeaker() {
    val c = colors()
    RemoteRow(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(6.rdp))
            .background(c.surface)
            .border(1.rdp, c.muted, RemoteRoundedCornerShape(6.rdp))
            .clickable(action { open })
            .padding(12.rdp),
        verticalAlignment = RemoteAlignment.CenterVertically,
        horizontalArrangement = RemoteArrangement.spacedBy(14.rdp),
    ) {
        RemoteBox(
            modifier = RemoteModifier.size(64.rdp, 64.rdp).clip(RemoteRoundedCornerShape(6.rdp)).background(c.primary).padding(2.rdp),
        ) {
            RemoteImage(
                bitmap = image { photo },
                contentDescription = null,
                modifier = RemoteModifier.fillMaxSize().clip(RemoteRoundedCornerShape(4.rdp)),
                contentScale = RemoteContentScale.Crop,
                alpha = 1f.rf,
            )
        }
        RemoteColumn(RemoteModifier.weight(1f), verticalArrangement = RemoteArrangement.spacedBy(2.rdp)) {
            Label("SPEAKER".rs, c.primary, size = 11)
            Label(text { name }, c.onSurface, size = 18, weight = FontWeight.Bold)
            Label(text { tagline }, c.muted, size = 13, weight = FontWeight.Normal, maxLines = 2)
            Label(text { sessions }, c.secondary, size = 12, weight = FontWeight.Medium)
        }
    }
}

@Composable
fun DroidconPromo() {
    val C = PromoContract
    val c = brandColors(Droidcon)
    RemoteColumn(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(8.rdp))
            .background(c.primary)
            .padding(16.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
    ) {
        RemoteRow(RemoteModifier.fillMaxWidth(), verticalAlignment = RemoteAlignment.CenterVertically) {
            Label("// ".rs + input(C.eyebrow), c.onPrimary, size = 12, modifier = RemoteModifier.weight(1f))
            RemoteBox(RemoteModifier.size(28.rdp, 28.rdp).clickable(emit(C.dismiss)), contentAlignment = RemoteAlignment.Center) {
                Label("✕".rs, c.onPrimary, size = 14)
            }
        }
        Label(input(C.headline), c.onPrimary, size = 22, weight = FontWeight.ExtraBold, maxLines = 2)
        Label(input(C.body), c.onPrimary, size = 14, weight = FontWeight.Normal, maxLines = 3)
        RemoteBox(
            modifier = RemoteModifier
                .padding(top = 6.rdp)
                .clip(RemoteRoundedCornerShape(4.rdp))
                .background(c.onPrimary)
                .clickable(emit(C.ctaAction, input(C.url)))
                .padding(horizontal = 14.rdp, vertical = 8.rdp),
        ) { Label(input(C.cta), c.primary, size = 13) }
    }
}

/** The next session, on a launcher widget: as much as fits, nothing that needs a scroll. */
@Composable
fun DroidconSessionWidget() {
    val C = SessionContract
    val c = brandColors(Droidcon)
    RemoteColumn(
        modifier = RemoteModifier
            .fillMaxSize()
            .clip(RemoteRoundedCornerShape(16.rdp))
            .background(c.surface)
            .clickable(emit(C.open))
            .padding(14.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(4.rdp),
    ) {
        Label("> NEXT UP · ".rs + input(C.time), c.primary, size = 12)
        // Two lines at most: at the launcher's nominal 120dp, a third leaves no room for the time.
        Label(input(C.title), c.onSurface, size = 16, weight = FontWeight.Bold, maxLines = 2)
        Label(input(C.room), c.muted, size = 12, weight = FontWeight.Medium)
    }
}

/** The next session on a watch: a round-safe, centred stack. */
@Composable
fun DroidconSessionWear() {
    val C = SessionContract
    val c = brandColors(Droidcon)
    RemoteColumn(
        modifier = RemoteModifier.fillMaxSize().background(c.surface).clickable(emit(C.open)).padding(12.rdp),
        horizontalAlignment = RemoteAlignment.CenterHorizontally,
        verticalArrangement = RemoteArrangement.Center,
    ) {
        Label(input(C.time), c.primary, size = 13)
        Label(input(C.title), c.onSurface, size = 15, weight = FontWeight.Bold, maxLines = 2)
        Label(input(C.room), c.muted, size = 12, weight = FontWeight.Medium)
    }
}

