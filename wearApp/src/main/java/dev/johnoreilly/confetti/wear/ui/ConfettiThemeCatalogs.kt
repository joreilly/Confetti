package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import ee.schimke.composeai.preview.WearThemeCatalog

/**
 * `@WearThemeCatalog` providers — Confetti Wear's **alternative themes**, declared so the preview
 * server can re-render *any* preview under any of them.
 *
 * Where [ThemeFoundationPreviews] bakes one static specimen sticker per theme, these providers make
 * the axis interactive: `compose-preview serve` lifts every `@WearThemeCatalog`-annotated
 * [PreviewWrapperProvider] into the viewer's **Theme** select, and picking one re-renders the
 * currently open preview through that provider's `Wrap` — so you can look at `SessionCard` or
 * `HomeScreen` in KotlinConf purple, then flip to DevFest blue, and see the live result rather than
 * a pre-baked PNG. (Daemon-backed: the control is live on a local `serve` session, disabled on a
 * published static bundle.)
 *
 * The **Wear** annotation, not the mobile `@ThemeCatalog`: these providers install
 * `androidx.wear.compose.material3.MaterialTheme`, so the auto-generated specimen sheet has to read
 * the Wear `ColorScheme` (`primaryDim`, `surfaceContainer*`, no `surfaceVariant` ramp). Annotated
 * with the mobile one, every sheet would silently report the baseline mobile M3 palette instead of
 * the theme it declares.
 *
 * Each wraps [ConfettiConferenceTheme] with a real conference id, so these are exactly the themes
 * the app resolves at runtime via [conferenceThemeFor] — the ids are prefix-matched, so a rolling
 * edition can't drift them. `group = "Conference"` buckets the four curated identities into their
 * own `<optgroup>` beneath the stock Confetti theme.
 */
@WearThemeCatalog(name = "Confetti (default)", group = "Confetti")
class ConfettiDefaultThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiConferenceTheme(conferenceId = null, content = content)
}

@WearThemeCatalog(name = "KotlinConf", group = "Conference")
class KotlinConfThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiConferenceTheme(conferenceId = "kotlinconf2025", content = content)
}

@WearThemeCatalog(name = "AndroidMakers", group = "Conference")
class AndroidMakersThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiConferenceTheme(conferenceId = "androidmakers2025", content = content)
}

@WearThemeCatalog(name = "Droidcon", group = "Conference")
class DroidconThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiConferenceTheme(conferenceId = "droidconlondon2025", content = content)
}

@WearThemeCatalog(name = "DevFest", group = "Conference")
class DevFestThemeCatalog : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) =
        ConfettiConferenceTheme(conferenceId = "devfest2025", content = content)
}
