package dev.johnoreilly.confetti.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import dev.johnoreilly.confetti.decompose.DarkThemeConfig
import ee.schimke.composeai.preview.ThemeCatalog

/**
 * `@ThemeCatalog` providers — the phone app's **alternative themes**, declared so the preview
 * server can re-render *any* preview under any of them.
 *
 * Where [ThemeFoundationPreviews] bakes one static specimen sticker per theme, these providers make
 * the axis interactive: `compose-preview serve` lifts every `@ThemeCatalog`-annotated
 * [PreviewWrapperProvider] into the viewer's **Theme** select, and picking one re-renders the
 * currently open preview through that provider's `Wrap` — so you can open `ScheduleScreenPreview`
 * and flip it between the brand purple, the Android green, Material You and a conference seed, and
 * see the live result rather than a pre-baked PNG. (Daemon-backed: the control is live on a local
 * `serve` session, disabled on a published static bundle.)
 *
 * Light and dark are declared as separate providers rather than left to `isSystemInDarkTheme()`:
 * the theme select is a *discrete* axis applied on top of whatever `uiMode` the preview renders
 * with, so a provider that read the ambient mode would make half the options no-ops. Each wraps a
 * production theme function — [ConfettiTheme] for the three colour schemes, [ConferenceMaterialTheme]
 * for the seeded app-root theme — so these can't drift from what the app renders.
 */
@ThemeCatalog(name = "Confetti brand Light", group = "Confetti")
class ConfettiBrandLightThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiTheme(darkTheme = false, disableDynamicTheming = true, content = content)
}

@ThemeCatalog(name = "Confetti brand Dark", group = "Confetti")
class ConfettiBrandDarkThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiTheme(darkTheme = true, disableDynamicTheming = true, content = content)
}

@ThemeCatalog(name = "Android Light", group = "Android")
class AndroidLightThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiTheme(darkTheme = false, androidTheme = true, content = content)
}

@ThemeCatalog(name = "Android Dark", group = "Android")
class AndroidDarkThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiTheme(darkTheme = true, androidTheme = true, content = content)
}

@ThemeCatalog(name = "Dynamic Light", group = "Material You")
class DynamicLightThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiTheme(darkTheme = false, disableDynamicTheming = false, content = content)
}

@ThemeCatalog(name = "Dynamic Dark", group = "Material You")
class DynamicDarkThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiTheme(darkTheme = true, disableDynamicTheming = false, content = content)
}

/**
 * Conference seed colours offered as themes. These are the identities the Wear app curates in
 * `dev.johnoreilly.confetti.wear.ui.ConferenceTheme`; on the phone the same colours arrive as the
 * backend's `conference.themeColor`, so wrapping [ConferenceMaterialTheme] with them previews
 * exactly what a phone user sees for that conference. The `0x` prefix is part of the wire format.
 */
private const val KOTLINCONF_SEED = "0xFF7F52FF"
private const val ANDROIDMAKERS_SEED = "0xFFE59A4F"
private const val DROIDCON_SEED = "0xFF00D775"
private const val DEVFEST_SEED = "0xFF4285F4"

/**
 * Shared body for the seeded providers. Written as a plain `@Composable` helper each provider calls
 * rather than something the providers delegate to with `by`: Kotlin's interface delegation
 * generates a bridge method, and a `@Composable` member is not safely delegable that way.
 */
@Composable
private fun ConferenceSeed(seed: String, dark: Boolean, content: @Composable () -> Unit) =
    ConferenceMaterialTheme(
        seedColorString = seed,
        darkThemeConfig = if (dark) DarkThemeConfig.DARK else DarkThemeConfig.LIGHT,
        content = content,
    )

@ThemeCatalog(name = "KotlinConf Light", group = "Conference")
class KotlinConfLightThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConferenceSeed(KOTLINCONF_SEED, dark = false, content = content)
}

@ThemeCatalog(name = "KotlinConf Dark", group = "Conference")
class KotlinConfDarkThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConferenceSeed(KOTLINCONF_SEED, dark = true, content = content)
}

@ThemeCatalog(name = "AndroidMakers Light", group = "Conference")
class AndroidMakersLightThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConferenceSeed(ANDROIDMAKERS_SEED, dark = false, content = content)
}

@ThemeCatalog(name = "AndroidMakers Dark", group = "Conference")
class AndroidMakersDarkThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConferenceSeed(ANDROIDMAKERS_SEED, dark = true, content = content)
}

@ThemeCatalog(name = "Droidcon Light", group = "Conference")
class DroidconLightThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConferenceSeed(DROIDCON_SEED, dark = false, content = content)
}

@ThemeCatalog(name = "Droidcon Dark", group = "Conference")
class DroidconDarkThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConferenceSeed(DROIDCON_SEED, dark = true, content = content)
}

@ThemeCatalog(name = "DevFest Light", group = "Conference")
class DevFestLightThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConferenceSeed(DEVFEST_SEED, dark = false, content = content)
}

@ThemeCatalog(name = "DevFest Dark", group = "Conference")
class DevFestDarkThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConferenceSeed(DEVFEST_SEED, dark = true, content = content)
}
