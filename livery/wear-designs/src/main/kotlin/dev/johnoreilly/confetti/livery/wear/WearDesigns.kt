package dev.johnoreilly.confetti.livery.wear

import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteImage
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.clickable
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.drawWithContent
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.shapes.RemoteCircleShape
import androidx.compose.remote.creation.compose.shapes.RemoteShape
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberRemoteImageBitmap
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.remote.material3.RemoteCardDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText
import androidx.wear.compose.remote.material3.RemoteTitleCard

/*
 * Each series' designs for a watch: the same contracts as the phone's (session card, section
 * header, session and speaker page headers), drawn with Wear's own Material 3 components
 * (remote-material3: RemoteMaterialTheme, RemoteTitleCard, RemoteText in the Wear type scale), so
 * on a watch they look like the Wear app around them, in the conference's colours.
 *
 * Colours: Wear's default scheme with the roles a series' theme pack sets swapped for named
 * `theme.*` values, so the pack (and a refreshed one) recolours them like the phone designs. A
 * pack's `surface` is a card's colour (as Livery's toLiveryTheme sends it), so it is the cards'
 * container here.
 */

/** How a series draws on a watch: its pack's colours (the initial values) and its accents. */
class WearBrand(
    val series: String,
    /** The series' dark theme pack (watches are dark): `theme.<role>` → ARGB. */
    val pack: Map<String, Int>,
    val style: WearStyle,
)

/**
 * The few things that set a series apart on the watch, beside its backdrop (WearBackdrops.kt); the
 * rest is Wear Material 3.
 */
enum class WearStyle(
    /** The series' name as its designs' titles give it. */
    val title: String,
    /** Around a section header's title, as the phone's section headers draw it. */
    val sectionPrefix: String,
    val sectionSuffix: String,
    /** Before the time on a session card. */
    val timePrefix: String,
) {
    /** Confetti's base catalog: Wear Material 3 in the conference's colours, with confetti. */
    Confetti(title = "Confetti", sectionPrefix = "", sectionSuffix = "", timePrefix = ""),

    /** droidcon: the site's bracketed labels, green rule and dot matrix. */
    Droidcon(title = "droidcon", sectionPrefix = "( ", sectionSuffix = " )", timePrefix = "> "),

    /** KotlinConf: the brand gradient, as glows and the cards' outline. */
    KotlinConf(title = "KotlinConf", sectionPrefix = "", sectionSuffix = "", timePrefix = ""),
    ;

    companion object {
        /** A catalog's series' style; the base one's for any other. */
        fun of(series: String): WearStyle = when (series) {
            "droidcon" -> Droidcon
            "kotlinconf" -> KotlinConf
            else -> Confetti
        }
    }
}

/** Wear Material 3 in [brand]'s colours, as named theme roles a theme pack replaces. */
@Composable
fun WearBrandTheme(v: WearValues, brand: WearBrand, content: @Composable () -> Unit) {
    val base = RemoteMaterialTheme.colorScheme
    fun role(name: String) = v.color("theme.$name", Color(brand.pack["theme.$name"] ?: 0xFF888888.toInt()))
    RemoteMaterialTheme(
        colorScheme = base.copy(
            primary = role("primary"),
            onPrimary = role("onPrimary"),
            secondary = role("secondary"),
            tertiary = role("tertiary"),
            surfaceContainer = role("surface"),
            onSurface = role("onSurface"),
            onSurfaceVariant = role("onSurfaceVariant"),
        ),
        content = content,
    )
}


/**
 * A panel in [brand]'s backdrop (WearBackdrops.kt): clipped to [shape], the series' backdrop drawn
 * under whatever the panel holds.
 */
@Composable
private fun RemoteModifier.brandPanel(brand: WearBrand, shape: RemoteShape): RemoteModifier {
    val colors = backdropColors()
    return clip(shape).drawWithContent {
        backdrop(brand.style, colors)
        drawContent()
    }
}

/**
 * A session in a list (`confetti/session@1`): Wear's title card, the title over the time, the
 * speakers and the room, and a star for the bookmark.
 */
@Composable
fun WearSessionCard(v: WearValues, brand: WearBrand) = WearBrandTheme(v, brand) {
    val bookmarked = v.bool("bookmarked")
    val colors = RemoteMaterialTheme.colorScheme
    val shape = RemoteCardDefaults.shape
    RemoteTitleCard(
        onClick = v.action("open"),
        // The series' backdrop under the card, which draws no container of its own over it.
        modifier = RemoteModifier.fillMaxWidth().brandPanel(brand, shape),
        shape = shape,
        colors = RemoteCardDefaults.cardColors(containerColor = Transparent),
        border = outlineFor(brand.style, backdropColors()),
        // The time on a line of its own (not the card's time slot, which on a watch leaves the
        // title half the width): the title gets the card's full width.
        title = { RemoteText(v.string("title"), maxLines = 2, overflow = TextOverflow.Ellipsis) },
    ) {
        RemoteRow(RemoteModifier.fillMaxWidth(), verticalAlignment = RemoteAlignment.CenterVertically) {
            RemoteColumn(RemoteModifier.weight(1f)) {
                RemoteText(brand.style.timePrefix.rs + v.string("time"), color = colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                RemoteText(v.string("speakers"), color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                RemoteText(v.string("room"), color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            RemoteBox(
                modifier = RemoteModifier.size(32.rdp, 32.rdp).clip(RemoteCircleShape).clickable(v.action("bookmark")),
                contentAlignment = RemoteAlignment.Center,
            ) {
                RemoteText(bookmarked.select("★".rs, "☆".rs), color = bookmarked.select(colors.primary, colors.onSurfaceVariant))
            }
        }
    }
}

/** A list's section header (`confetti/section@1`): Wear's list sub-header, in the primary colour. */
@Composable
fun WearSection(v: WearValues, brand: WearBrand) = WearBrandTheme(v, brand) {
    val colors = backdropColors()
    RemoteBox(
        RemoteModifier.fillMaxSize()
            .drawWithContent {
                sectionMarks(brand.style, colors)
                drawContent()
            }
            .padding(horizontal = 28.rdp),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteText(
            brand.style.sectionPrefix.rs + v.string("title") + brand.style.sectionSuffix.rs,
            color = RemoteMaterialTheme.colorScheme.primary,
            style = RemoteMaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The top of a session's page (`confetti/session-header@1`): the title centred, as Wear's screen
 * header, the time and the room under it. The room is text: a watch has no venue map to open, so
 * the design sends no `openRoom`.
 */
@Composable
fun WearSessionHeader(v: WearValues, brand: WearBrand) = WearBrandTheme(v, brand) {
    val colors = RemoteMaterialTheme.colorScheme
    RemoteColumn(
        modifier = RemoteModifier.fillMaxSize()
            .brandPanel(brand, RemoteCardDefaults.shape)
            .padding(horizontal = 14.rdp, vertical = 12.rdp),
        horizontalAlignment = RemoteAlignment.CenterHorizontally,
        verticalArrangement = RemoteArrangement.spacedBy(4.rdp, RemoteAlignment.CenterVertically),
    ) {
        RemoteText(
            v.string("title"),
            color = colors.onSurface,
            style = RemoteMaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        RemoteText(brand.style.timePrefix.rs + v.string("time"), color = colors.primary, textAlign = TextAlign.Center)
        RemoteText(v.string("room"), color = colors.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * The top of a speaker's page (`confetti/speaker-header@1`): the photo in a circle (in a ring of
 * the series' accent), the name and tagline centred, as the Wear app's own
 * speaker page has them. No links and no full-screen photo: a watch opens neither, so the design
 * sends neither action.
 */
@Composable
fun WearSpeakerHeader(v: WearValues, brand: WearBrand) = WearBrandTheme(v, brand) {
    val colors = RemoteMaterialTheme.colorScheme
    val backdrop = backdropColors()
    RemoteColumn(
        modifier = RemoteModifier.fillMaxSize()
            .brandPanel(brand, RemoteCardDefaults.shape)
            .padding(horizontal = 14.rdp, vertical = 10.rdp),
        horizontalAlignment = RemoteAlignment.CenterHorizontally,
        verticalArrangement = RemoteArrangement.spacedBy(4.rdp, RemoteAlignment.CenterVertically),
    ) {
        // The photo in a ring of the series' accent: Confetti's three colours, droidcon's green,
        // KotlinConf's gradient.
        RemoteBox(
            modifier = RemoteModifier
                .size(80.rdp, 80.rdp)
                .clip(RemoteCircleShape)
                .background(ringBrush(brand.style, backdrop))
                .padding(3.rdp),
        ) {
            RemoteImage(
                remoteBitmap = rememberRemoteImageBitmap(url = "livery:value/photo"),
                contentDescription = v.string("name"),
                modifier = RemoteModifier.fillMaxSize().clip(RemoteCircleShape).background(colors.surfaceContainer),
                contentScale = ContentScale.Crop,
            )
        }
        RemoteText(v.string("name"), color = colors.onSurface, style = RemoteMaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        RemoteText(v.string("tagline"), color = colors.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/** Every Wear design's height when captured, in dp: a slot is as tall as its document. */
object WearHeights {
    const val SESSION = 132
    const val SECTION = 32
    const val SESSION_HEADER = 140
    const val SPEAKER_HEADER = 196
}

/** The width a watch's list item has, in dp: a large round watch's, inside its insets. */
const val WEAR_WIDTH = 200
