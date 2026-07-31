package dev.johnoreilly.confetti.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * The switch that lets preview tooling replace a preview's theme.
 *
 * **The problem this solves.** Every design-catalog `@Preview` installs Confetti's theme in its own
 * body — `ConfettiThemeFixed { … }` on Wear, `CatalogTheme { … }` on the phone. That is deliberate:
 * it is what makes the Android Studio preview pane and a plain `compose-preview` render show the
 * real app theme with no annotation and no tooling in the loop. But `compose-preview serve` applies
 * an alternative theme by *wrapping* the preview function in a `@ThemeCatalog` provider, and a theme
 * installed in the body composes **inside** that wrapper and shadows it. Every entry in the viewer's
 * **Theme** select therefore rendered byte-identical pixels — the switcher looked live and did
 * nothing.
 *
 * **The mechanism.** The theme-catalog providers install their theme and then mark it as an override
 * with [PreviewThemeOverrideInstalled]. Confetti's own theme functions check
 * [LocalPreviewThemeOverride] and, when it is set, step aside rather than installing a second
 * `MaterialTheme` over the top. The default theme stays exactly where it is — in the preview body —
 * so nothing changes for Studio, for the CLI, or for the app itself; the theme only yields when
 * something outside deliberately claimed it.
 *
 * **Yielding consumes the flag** ([ConsumePreviewThemeOverride]), so only the *outermost* app theme
 * steps aside. A deliberately nested theme deeper in the tree — `ConferencesView` tinting each row
 * with that conference's seed colour, say — still installs, so an override re-themes the base
 * without flattening intentional per-item theming.
 *
 * In production nothing ever provides `true`: the default is `false`, so `ConfettiTheme` and friends
 * install normally and this costs a single composition-local read.
 *
 * Adding a new theme function to the app? Give it the same two-line check, or the preview theme
 * switcher will silently stop working for anything that uses it.
 */
val LocalPreviewThemeOverride: ProvidableCompositionLocal<Boolean> =
    compositionLocalOf { false }

/**
 * Marks the theme installed by the enclosing `@ThemeCatalog` / `@WearThemeCatalog` provider as an
 * override, so the app theme inside [content] stands down instead of shadowing it.
 *
 * Call this **inside** the provider's own theme, wrapping the preview content — not around the
 * provider's theme call, which would make that theme stand down too.
 */
@Composable
fun PreviewThemeOverrideInstalled(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPreviewThemeOverride provides true, content = content)
}

/**
 * Runs [content] with the override flag cleared — what an app theme function calls when it stands
 * down, so that only the outermost one does and deliberate nested theming still applies.
 */
@Composable
fun ConsumePreviewThemeOverride(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPreviewThemeOverride provides false, content = content)
}
