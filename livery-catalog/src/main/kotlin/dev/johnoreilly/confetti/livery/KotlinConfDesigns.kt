package dev.johnoreilly.confetti.livery

import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteContentScale
import androidx.compose.remote.creation.compose.layout.RemoteImage
import androidx.compose.remote.creation.compose.layout.RemoteOffset
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.border
import androidx.compose.remote.creation.compose.modifier.clickable
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.rotate
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.modifier.visibility
import androidx.compose.remote.creation.compose.shapes.RemoteCircleShape
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.shaders.RemoteBrush
import androidx.compose.remote.creation.compose.shaders.linearGradient
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteTileMode
import androidx.compose.remote.creation.compose.state.RemoteTimeVariables
import androidx.compose.remote.creation.compose.state.cos
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.sin
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/*
 * KotlinConf: the Kotlin gradient (purple → magenta → coral), big soft corners, white type, and a
 * gradient that drifts slowly on the player's clock.
 */

/** KotlinConf's designs, captured into catalogs/kotlinconf. */
val KotlinConfDesigns: List<DesignDef> = designs(
    KotlinConf, "kotlinconf", "KotlinConf",
    header = { KotlinConfHeader() },
    session = { KotlinConfSession() },
    speaker = { KotlinConfSpeaker() },
    promo = { KotlinConfPromo() },
    // Poppins' tall line height: at the contracts' limits (their `long` examples) a two-line name,
    // title, tagline or body needs the room for the row below it (location, tag, sessions, CTA).
    headerHeightDp = 290,
    sessionHeightDp = 170,
    speakerHeightDp = 260,
    promoHeightDp = 275,
    sessionWidget = { KotlinConfSessionWidget() },
    sessionWear = { KotlinConfSessionWear() },
)

private val White = Color.White

/** The Kotlin gradient, its direction turning once every ~30 s. */
private fun kotlinGradient(c: BrandColors, drift: Boolean = true): RemoteBrush {
    val t = RemoteTimeVariables.animationTime * 0.2f
    val dx = if (drift) cos(t) * 300f else 300f.rf
    val dy = if (drift) sin(t) * 300f else 300f.rf
    return RemoteBrush.linearGradient(
        colors = listOf(c.primary, c.secondary, c.tertiary),
        start = RemoteOffset(0f.rf, 0f.rf),
        end = RemoteOffset(dx + 300f, dy + 120f),
        tileMode = RemoteTileMode.Mirror,
    )
}

@Composable
fun KotlinConfHeader() {
    val C = ConferenceContract
    val c = brandColors(KotlinConf)
    val onBrand = c.onPrimary
    RemoteBox(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(24.rdp))
            // The Kotlin colours as a slow mesh rather than a straight gradient: purple underneath,
            // magenta and coral light drifting across it.
            .background(c.primary)
            .meshGlows(listOf(
                Glow(c.secondary.copy(alpha = 0.85f.rf), x = 300f, y = 40f, radius = 230f, drift = 50f, periodSeconds = 28f),
                Glow(c.tertiary.copy(alpha = 0.75f.rf), x = 50f, y = 180f, radius = 200f, drift = 50f, periodSeconds = 36f, phase = 2.5f),
                Glow(c.secondary.copy(alpha = 0.35f.rf), x = 120f, y = 0f, radius = 160f, drift = 60f, periodSeconds = 22f, phase = 5f),
            )),
    ) {
        // A slowly turning "K" diamond in the corner: decoration, on the player's clock.
        RemoteBox(RemoteModifier.fillMaxWidth().padding(top = 14.rdp, end = 18.rdp), contentAlignment = RemoteAlignment.TopEnd) {
          RemoteBox(
            modifier = RemoteModifier
                .size(88.rdp, 88.rdp)
                .rotate(RemoteTimeVariables.animationTime * 12f)
                .clip(RemoteRoundedCornerShape(18.rdp))
                .border(6.rdp, onBrand.copy(alpha = 0.35f.rf), RemoteRoundedCornerShape(18.rdp)),
          )
        }
        RemoteColumn(
            modifier = RemoteModifier.fillMaxWidth().padding(22.rdp),
            verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
        ) {
            RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically, horizontalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
                Label(input(C.dates), onBrand, size = 14, weight = FontWeight.SemiBold)
                LivePill(inputBool(C.live), onBrand, c.primary, text = "Live now")
            }
            Label(input(C.name), onBrand, size = 32, weight = FontWeight.Black, maxLines = 2)
            Label(input(C.tagline), onBrand, size = 15, weight = FontWeight.Normal, maxLines = 1)
            // The venue and the tickets button on lines of their own: side by side, a long venue
            // pushed the button out of the card.
            RemoteColumn(
                modifier = RemoteModifier.padding(top = 8.rdp),
                verticalArrangement = RemoteArrangement.spacedBy(8.rdp),
            ) {
                Chip(input(C.location), onBrand.copy(alpha = 0.2f.rf), onBrand)
                // Only when the app has somewhere to send people: no URL, no button.
                val ticketsUrl = input(C.ticketsUrl)
                RemoteBox(
                    modifier = RemoteModifier
                        .visibility(ticketsUrl.isNotEmpty.visibleIf())
                        .clip(RemoteCircleShape)
                        .background(onBrand)
                        .clickable(emit(C.tickets, ticketsUrl))
                        .padding(horizontal = 14.rdp, vertical = 5.rdp),
                ) { Label("Get tickets".rs, c.primary, size = 13) }
            }
        }
    }
}

@Composable
fun KotlinConfSession() {
    val C = SessionContract
    val c = brandColors(KotlinConf)
    val bookmarked = inputBool(C.bookmarked)
    RemoteRow(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(20.rdp))
            .background(c.surface)
            .border(1.5f.rdp, c.primary, RemoteRoundedCornerShape(20.rdp))
            .clickable(emit(C.open))
            .padding(16.rdp),
        horizontalArrangement = RemoteArrangement.spacedBy(12.rdp),
    ) {
        RemoteColumn(RemoteModifier.weight(1f), verticalArrangement = RemoteArrangement.spacedBy(6.rdp)) {
            RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically, horizontalArrangement = RemoteArrangement.spacedBy(8.rdp)) {
                Chip(input(C.time), c.primary, c.onPrimary)
                LivePill(inputBool(C.live), c.tertiary, c.onPrimary, text = "Live")
                Label(input(C.room), c.muted, size = 13, weight = FontWeight.Medium, modifier = RemoteModifier.weight(1f))
            }
            Label(input(C.title), c.onSurface, size = 18, weight = FontWeight.Bold, maxLines = 2)
            Label(input(C.speakers), c.muted, size = 14, weight = FontWeight.Normal)
            Label(input(C.tag), c.secondary, size = 12, weight = FontWeight.SemiBold)
        }
        RemoteBox(
            modifier = RemoteModifier
                .size(40.rdp, 40.rdp)
                .clip(RemoteCircleShape)
                .background(bookmarked.select(c.primary, c.surface))
                .border(1.5f.rdp, c.primary, RemoteCircleShape)
                .clickable(emit(C.bookmark)),
            contentAlignment = RemoteAlignment.Center,
        ) {
            Label(bookmarked.select("♥".rs, "♡".rs), bookmarked.select(c.onPrimary, c.onSurface), size = 18)
        }
    }
}

@Composable
fun KotlinConfSpeaker() {
    val C = SpeakerContract
    val c = brandColors(KotlinConf)
    RemoteColumn(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(24.rdp))
            .background(c.surface)
            .clickable(emit(C.open))
            .padding(18.rdp),
        horizontalAlignment = RemoteAlignment.CenterHorizontally,
        verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
    ) {
        // Gradient ring around the photo.
        RemoteBox(
            modifier = RemoteModifier.size(96.rdp, 96.rdp).clip(RemoteCircleShape).background(kotlinGradient(c)).padding(4.rdp),
        ) {
            RemoteImage(
                bitmap = inputImage(C.photo),
                contentDescription = null,
                modifier = RemoteModifier.fillMaxSize().clip(RemoteCircleShape).background(c.surface),
                contentScale = RemoteContentScale.Crop,
                alpha = 1f.rf,
            )
        }
        Label(input(C.name), c.onSurface, size = 20, weight = FontWeight.Bold)
        Label(input(C.tagline), c.muted, size = 14, weight = FontWeight.Normal, maxLines = 2)
        Chip(input(C.sessions), c.primary, c.onPrimary)
    }
}

@Composable
fun KotlinConfPromo() {
    val C = PromoContract
    val c = brandColors(KotlinConf)
    val onBrand = c.onPrimary
    RemoteColumn(
        modifier = RemoteModifier
            .fillMaxWidth()
            .clip(RemoteRoundedCornerShape(24.rdp))
            .background(kotlinGradient(c))
            .padding(18.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
    ) {
        RemoteRow(RemoteModifier.fillMaxWidth(), verticalAlignment = RemoteAlignment.CenterVertically) {
            Label(input(C.eyebrow), onBrand, size = 13, weight = FontWeight.SemiBold, modifier = RemoteModifier.weight(1f))
            RemoteBox(
                RemoteModifier.size(28.rdp, 28.rdp).clip(RemoteCircleShape).background(onBrand.copy(alpha = 0.2f.rf)).clickable(emit(C.dismiss)),
                contentAlignment = RemoteAlignment.Center,
            ) { Label("✕".rs, onBrand, size = 13) }
        }
        Label(input(C.headline), onBrand, size = 22, weight = FontWeight.Black, maxLines = 2)
        Label(input(C.body), onBrand, size = 14, weight = FontWeight.Normal, maxLines = 3)
        RemoteBox(
            modifier = RemoteModifier
                .padding(top = 6.rdp)
                .clip(RemoteCircleShape)
                .background(onBrand)
                .clickable(emit(C.ctaAction, input(C.url)))
                .padding(horizontal = 16.rdp, vertical = 8.rdp),
        ) { Label(input(C.cta), c.primary, size = 14) }
    }
}

@Composable
fun KotlinConfSessionWidget() {
    val C = SessionContract
    val c = brandColors(KotlinConf)
    val onBrand = c.onPrimary
    RemoteColumn(
        modifier = RemoteModifier
            .fillMaxSize()
            .clip(RemoteRoundedCornerShape(24.rdp))
            .background(kotlinGradient(c, drift = false))
            .clickable(emit(C.open))
            .padding(horizontal = 16.rdp, vertical = 12.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(2.rdp),
    ) {
        Label("Next · ".rs + input(C.time), onBrand, size = 12, weight = FontWeight.SemiBold)
        // Two lines at most: at the launcher's nominal 120dp, a third pushes the room out.
        Label(input(C.title), onBrand, size = 16, weight = FontWeight.Black, maxLines = 2)
        Label(input(C.room), onBrand, size = 12, weight = FontWeight.Medium)
    }
}

@Composable
fun KotlinConfSessionWear() {
    val C = SessionContract
    val c = brandColors(KotlinConf)
    val onBrand = c.onPrimary
    RemoteColumn(
        modifier = RemoteModifier.fillMaxSize().background(kotlinGradient(c, drift = false)).clickable(emit(C.open)).padding(14.rdp),
        horizontalAlignment = RemoteAlignment.CenterHorizontally,
        verticalArrangement = RemoteArrangement.Center,
    ) {
        Label(input(C.time), onBrand, size = 13, weight = FontWeight.SemiBold)
        Label(input(C.title), onBrand, size = 15, weight = FontWeight.Black, maxLines = 2)
        Label(input(C.room), onBrand, size = 12, weight = FontWeight.Medium)
    }
}


