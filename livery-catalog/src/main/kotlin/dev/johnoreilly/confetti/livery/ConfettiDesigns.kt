package dev.johnoreilly.confetti.livery

import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteContentScale
import androidx.compose.remote.creation.compose.layout.RemoteImage
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.clickable
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.modifier.visibility
import androidx.compose.remote.creation.compose.shapes.RemoteCircleShape
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/*
 * The base template: Confetti's own look, for every conference that has no catalog of its own.
 * Material-flavoured (tonal cards, rounded corners, the app's purple, orange and blue as theme
 * roles), so a conference without designs looks like Confetti, drawn by Livery.
 *
 * Each design is written against its contract through a context parameter: `text { title }`
 * reads the session contract's title, and another contract's input doesn't compile.
 */

/** The base catalog's designs, captured into catalogs/confetti. */
val ConfettiDesigns: List<DesignDef> = designs(
    Confetti, "default", "Confetti",
    header = { with(ContractScope(ConferenceContract, Confetti)) { ConfettiHeader() } },
    session = { with(ContractScope(SessionContract, Confetti)) { ConfettiSession() } },
    speaker = { with(ContractScope(SpeakerContract, Confetti)) { ConfettiSpeaker() } },
    promo = { with(ContractScope(PromoContract, Confetti)) { ConfettiPromo() } },
    // At the contracts' limits: a two-line name and tagline leave the tickets button no room at
    // 210dp, and a two-line headline and three-line body the promo's CTA none at 200dp.
    headerHeightDp = 250,
    promoHeightDp = 240,
)

@Composable
context(_: ContractScope<ConferenceContract>)
fun ConfettiHeader() {
    val c = colors()
    RemoteColumn(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(16.rdp))
            .background(c.primary)
            // A slow mesh of the brand's other two colours under the copy.
            .meshGlows(listOf(
                Glow(c.secondary.copy(alpha = 0.45f.rf), x = 310f, y = 10f, radius = 190f, periodSeconds = 34f),
                Glow(c.tertiary.copy(alpha = 0.4f.rf), x = 30f, y = 200f, radius = 180f, periodSeconds = 26f, phase = 2f),
                Glow(c.onPrimary.copy(alpha = 0.08f.rf), x = 180f, y = 100f, radius = 130f, drift = 60f, periodSeconds = 40f, phase = 4f),
            ))
            .padding(20.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
    ) {
        RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically, horizontalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
            Label(text { dates }, c.onPrimary, size = 13, weight = FontWeight.Medium)
            LivePill(flag { live }, c.onPrimary, c.primary, text = "Live")
        }
        Label(text { name }, c.onPrimary, size = 28, weight = FontWeight.Bold, maxLines = 2)
        Label(text { tagline }, c.onPrimary, size = 15, weight = FontWeight.Normal, maxLines = 2)
        Label(text { location }, c.onPrimary.copy(alpha = 0.8f.rf), size = 13, weight = FontWeight.Medium)
        // Only when the app has somewhere to send people: no URL, no button.
        val ticketsUrl = text { ticketsUrl }
        RemoteBox(
            modifier = RemoteModifier
                .visibility(ticketsUrl.isNotEmpty.visibleIf())
                .padding(top = 8.rdp)
                .clip(RemoteCircleShape)
                .background(c.onPrimary)
                .clickable(action({ tickets }, ticketsUrl))
                .padding(horizontal = 16.rdp, vertical = 6.rdp),
        ) { Label("Get tickets".rs, c.primary, size = 13, weight = FontWeight.SemiBold) }
    }
}

@Composable
context(_: ContractScope<SessionContract>)
fun ConfettiSession() {
    val c = colors()
    val bookmarked = flag { bookmarked }
    RemoteRow(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(12.rdp))
            .background(c.surface)
            .clickable(action { open }),
        verticalAlignment = RemoteAlignment.CenterVertically,
    ) {
        RemoteColumn(
            modifier = RemoteModifier.weight(1f).padding(start = 16.rdp, top = 12.rdp, end = 8.rdp, bottom = 12.rdp),
            verticalArrangement = RemoteArrangement.spacedBy(4.rdp),
        ) {
            RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically, horizontalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
                Label(text { time }, c.primary, size = 12, weight = FontWeight.SemiBold)
                LivePill(flag { live }, c.secondary, c.onPrimary, text = "Live")
                Label(text { room }, c.muted, size = 12, weight = FontWeight.Medium, modifier = RemoteModifier.weight(1f))
            }
            Label(text { title }, c.onSurface, size = 16, weight = FontWeight.SemiBold, maxLines = 2)
            Label(text { speakers }, c.muted, size = 13, weight = FontWeight.Normal)
            Chip(text { tag }, c.tertiary.copy(alpha = 0.15f.rf), c.tertiary, corner = 8)
        }
        RemoteBox(
            modifier = RemoteModifier.padding(8.rdp).size(40.rdp, 40.rdp).clickable(action { bookmark }),
            contentAlignment = RemoteAlignment.Center,
        ) {
            Label(bookmarked.select("★".rs, "☆".rs), bookmarked.select(c.primary, c.muted), size = 22)
        }
    }
}

@Composable
context(_: ContractScope<SpeakerContract>)
fun ConfettiSpeaker() {
    val c = colors()
    RemoteRow(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(12.rdp))
            .background(c.surface)
            .clickable(action { open })
            .padding(12.rdp),
        verticalAlignment = RemoteAlignment.CenterVertically,
        horizontalArrangement = RemoteArrangement.spacedBy(14.rdp),
    ) {
        RemoteImage(
            bitmap = image { photo },
            contentDescription = null,
            modifier = RemoteModifier.size(64.rdp, 64.rdp).clip(RemoteCircleShape),
            contentScale = RemoteContentScale.Crop,
            alpha = 1f.rf,
        )
        RemoteColumn(RemoteModifier.weight(1f), verticalArrangement = RemoteArrangement.spacedBy(2.rdp)) {
            Label(text { name }, c.onSurface, size = 18, weight = FontWeight.SemiBold)
            Label(text { tagline }, c.muted, size = 13, weight = FontWeight.Normal, maxLines = 2)
            Label(text { sessions }, c.primary, size = 12, weight = FontWeight.Medium)
        }
    }
}

@Composable
context(_: ContractScope<PromoContract>)
fun ConfettiPromo() {
    val c = colors()
    RemoteColumn(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(16.rdp))
            .background(c.surface)
            .padding(16.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
    ) {
        RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically) {
            Label(text { eyebrow }, c.secondary, size = 12, weight = FontWeight.SemiBold, modifier = RemoteModifier.weight(1f))
            RemoteBox(
                modifier = RemoteModifier.size(32.rdp, 32.rdp).clickable(action { dismiss }),
                contentAlignment = RemoteAlignment.Center,
            ) { Label("✕".rs, c.muted, size = 14) }
        }
        Label(text { headline }, c.onSurface, size = 20, weight = FontWeight.Bold, maxLines = 2)
        Label(text { body }, c.muted, size = 14, weight = FontWeight.Normal, maxLines = 3)
        val url = text { url }
        RemoteBox(
            modifier = RemoteModifier
                .padding(top = 6.rdp)
                .clip(RemoteCircleShape)
                .background(c.primary)
                .clickable(action({ ctaAction }, url))
                .padding(horizontal = 16.rdp, vertical = 8.rdp),
        ) { Label(text { cta }, c.onPrimary, size = 13, weight = FontWeight.SemiBold) }
    }
}
