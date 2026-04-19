# Confetti Style Guide

_A shared design vocabulary for the Confetti apps — currently focused on
Wear OS, with notes on mobile parity for when we bring the same treatment
to the phone app._

## 1. Intent

Confetti is a conference companion. Attendees glance at it between talks —
sometimes on a phone in the lobby, often on a wrist walking between rooms.
The UI's job is to compress a day of conference schedule into a few
confident glances: "what am I bookmarked for", "what's happening now",
"which hallway track is the next room". Every decision below serves that
intent.

On Wear specifically, Material 3 Expressive's four pillars — _modern,
relevant, distinct, expressive_ — point at the same goal. We lean on them
shamelessly: conference-specific seed colors, variable-axis typography
that reacts to motion, and a hierarchy that lets a half-second glance find
the next session without scanning a single line of small type.

## 2. Color system

### Source of truth: the seed

Every color on screen derives from a single `seedColor` — the hex color
stored on the active `GetConferencesQuery.Conference` entry
(`themeColor`). Each conference "brands" the app for the duration it's the
active one. When no conference is active (first launch), we fall back to
the Wear `ColorScheme()` defaults.

The mapping is done at theme-building time by
[materialkolor](https://github.com/jordond/MaterialKolor)'s
`rememberDynamicColorScheme`, configured for `isDark = true,
isAmoled = false`. The resulting Material 3 scheme is projected onto the
Wear Material 3 `ColorScheme` in [`ConfettiTheme.kt`](../wearApp/src/main/java/dev/johnoreilly/confetti/wear/ui/ConfettiTheme.kt)
via `toWearMaterialColors()`.

### Token → usage matrix

| Role | Token | Where it shows |
|---|---|---|
| **Conference accent** | `primary` | Conference name (HomeScreen title), section labels, primary CTAs |
| **Accent surfaces** | `primaryContainer` / `onPrimaryContainer` | Filled-variant buttons (day chips, conference chips) |
| **Surface base** | `background` (= M3 `surfaceContainerLowest`) | The watch-face backdrop; the bezel blends into this |
| **Card surfaces** | `surfaceContainerLow` → `surfaceContainerHigh` | `TitleCard`, chips, inline list items — low at rest, high when focussed |
| **Primary body text** | `onSurface` | Screen titles, card titles, primary list rows |
| **Secondary body text** | `onSurfaceVariant` | Speaker names, times, rooms, "no past sessions" empty states |
| **Dividers / strokes** | `outline`, `outlineVariant` | Subtle separators, placeholder edges |
| **Errors** | `error`, `onError`, `errorContainer` | "Now" session marker, failed-fetch banners |
| **Dimmed accent** | `primaryDim` / `secondaryDim` / `tertiaryDim` | Wear-specific dimmed roles; we alias them to the corresponding `*Container` tokens |

### Guidelines

- **Never hardcode a color.** If the `toWearMaterialColors()` map is missing
  a role you need, extend the map — don't paint directly with `Color(0xFF…)`
  inside a screen.
- **Primary is the seed's ambassador.** Promote it only for the connective
  tissue a user scans first (the conference name, section labels). Overuse
  turns every screen into a primary-coloured thicket.
- **Body text uses `onSurfaceVariant` below the headline.** This is the
  single biggest hierarchy lever on Wear — one line in `onSurface`, one in
  `onSurfaceVariant`, and the eye knows which to read first.
- **Cards sit on `surfaceContainerLow`.** `TitleCard` defaults work out of
  the box; don't override `CardDefaults.cardColors` unless you're building
  a one-off.

## 3. Typography

Confetti Wear ships a single typography — **Expressive**: Roboto Flex for
display, title, and numeral roles; Inter for body and label. This is the
Material 3 Expressive poster pairing, and it's hard-wired into the theme
(see [`Fonts.kt`](../wearApp/src/main/java/dev/johnoreilly/confetti/wear/ui/Fonts.kt)
and the per-variant
[`FontFamilies.kt`](../wearApp/src/debug/java/dev/johnoreilly/confetti/wear/ui/FontFamilies.kt)).
Debug builds bundle the variable TTFs in `res/font/` so Robolectric
previews render the real fonts; release builds download via the Google
Fonts provider to keep the APK small.

### Why Expressive

- **Roboto Flex's variable axes** (wght, opsz, grad, slnt, wdth) are
  designed to respond to motion and viewport shifts — exactly the
  conditions a `TransformingLazyColumn` produces as items scroll and
  morph. On a small round face the font's small-optical-size axis picks
  up extra contrast automatically.
- **Inter** is hinted for legibility at 12–14 dp. The conference data
  (speaker names, room names, abbreviated times) lives in that exact
  range, and Inter stays readable where the default Roboto can feel
  slightly thin.
- The pair is neutral enough that the active conference's seedColor
  (mapped onto `primary` and `surfaceContainer*`) is the visual signal
  carrying the brand — the typography doesn't fight it.

### Alternatives considered (archived, not in the code)

Three other stacks were built, rendered, and rejected. They're
referenced here so future design pivots have the prior work to lean on
rather than re-derive; the comparison renders are archived in
[PR #1680's preview-diff comment](https://github.com/joreilly/Confetti/pull/1680).

| Alternative | Display / Title | Body / Label | Why not shipped |
|---|---|---|---|
| **System** | Roboto (Wear default) | Roboto | Safest but the hierarchy lacks identity; the seedColor alone has to carry the conference brand. |
| **Editorial** | Newsreader (serif) | Public Sans | Magazine-weight conference names are striking but serifs at 14 dp on a round face trade glanceability for aesthetics. |
| **Confident** | Space Grotesk | Inter | "Dev conference branding" energy, but Space Grotesk's wide counters eat horizontal room we need for session titles. |

### Role mapping

All four stacks preserve the Wear Material 3 scale (same font sizes, line
heights, weights) — the only thing that changes is the family. This keeps
layout deterministic so a typography switch never reflows the whole screen.

| Role | Used for |
|---|---|
| `titleLarge` | Conference name (HomeScreen) |
| `titleMedium` | Screen titles (`ScreenHeader`) |
| `titleSmall` | Card titles (`SessionCard`) |
| `labelMedium` | Section labels (`SectionHeader`, below headers) |
| `labelSmall` | Version string, dev-mode text |
| `bodyMedium` | Empty-state messages, body copy |
| `bodySmall` | Speaker names under session titles |
| `arc*` / `numeral*` | Left to the Wear defaults — curved time text and tile numerals don't switch families |

### Notes

- **Offline fallback is automatic.** `FontFamily` holds a chain; if the
  GoogleFonts download hasn't arrived, the Compose font loader substitutes a
  system face of the same weight. Users never see a blank glyph.
- **Variable axes only light up on Roboto Flex.** The Expressive pairing
  is the only one that animates weight during a scroll — if you see the
  conference name morph slightly as `TransformingLazyColumn` scrolls, that's
  intentional and comes from the Wear compose-material3 library itself, not
  our code.

## 4. Conference themes

A confectti-running conference is a brand. KotlinConf's JetBrains purple,
Droidcon's bright green, DevFest's Google Blue — these are recognisable on
their own and the watch app should carry them visibly, not bury them behind
a neutral house style.

Four conferences get a curated [`ConferenceTheme`](../wearApp/src/main/java/dev/johnoreilly/confetti/wear/ui/ConferenceTheme.kt):

| Conference | Seed | Icon | Typography | Signature |
|---|---|---|---|---|
| **KotlinConf** | `#7F52FF` (JetBrains purple — the warm end of their red→purple gradient) | `Icons.Filled.Code` (⟨⟩) | **JetBrains Mono titles + Inter body** | JetBrains' own OFL-licensed monospace for titles gives the conference name a terminal/IDE feel; Inter keeps session cards readable |
| **Android Makers** | `#E59A4F` (Parisian ochre, leaning into the droidcon-Paris venue palette) | `Icons.Filled.Android` | Expressive (default) | Warm ochre day chips + bug-droid iconography |
| **Droidcon** | `#00D775` (their distinctive bright green) | `Icons.Filled.Adb` (droid bot) | Expressive (default) | Green dominant — the whole identity runs on it |
| **DevFest** | `#4285F4` (Google Blue) | `Icons.Filled.Celebration` | **Google Sans Flex** | Google's own brand family, OFL-licensed since late 2025 |

Everything else falls back to whatever `themeColor` the backend ships on
the conference and the default Expressive typography — the lookup is a
single switch in
[`conferenceThemeFor()`](../wearApp/src/main/java/dev/johnoreilly/confetti/wear/ui/ConferenceTheme.kt)
keyed on a case-insensitive prefix of the conference id, so
`kotlinconf2024`, `kotlinconf2025`, `kotlinconf2026` all pick up the same
identity.

### How the theme flows

1. [`ConfettiApp`](../wearApp/src/main/java/dev/johnoreilly/confetti/wear/ui/ConfettiApp.kt) resolves `conferenceThemeFor(appState.defaultConference)`.
2. If there's a curated theme, its `seedColor` and (optional) `typography`
   override the backend-supplied values on `ConfettiTheme`.
3. [`HomeScreen`'s title section](../wearApp/src/main/java/dev/johnoreilly/confetti/wear/home/HomeScreen.kt) asks the same lookup for the current conference id and renders
   `theme.icon` above the name when one exists.

That's the full extent of the conference-aware code. No per-conference
components, no custom drawables, no bespoke screens. The identity is
carried entirely by tokens + a single icon + (for DevFest) a typography
swap. The point is that adding a new curated conference is one new branch
of `conferenceThemeFor()` and nothing else.

### What we deliberately didn't do

- **No gradient fills** even though KotlinConf's brand gradient is
  beautiful. Watch faces clip to the inscribed circle; a gradient at that
  scale reads as noise.
- **No custom logos**. Material icons (`Code`, `Android`, `Adb`,
  `Celebration`) are recognisable enough at 16 dp and they update for free
  as Material evolves. A bespoke bundled SVG per conference is a
  maintenance obligation we don't need yet.
- **No per-conference layout changes**. Every conference gets the same
  Home / Bookmarks / Sessions shape. Identity travels through color,
  icon, and (sparingly) typography — not structure.
- **No fully-monospaced conferences.** JetBrains Mono on titles is a
  flourish; applying it to body + labels as well would get tiring to read
  at 12–14 dp on a round face. The Inter body keeps session cards
  breathing. Same principle would apply to any future conference that
  wants a display-only brand face.

### Signature flourishes, used sparingly

These are the one-off visual cues each conference leans on in the wild.
We don't reproduce them literally — the notes are design context for
future pivots.

- **KotlinConf**: red→purple gradient. If we ever want a louder accent,
  a single gradient `EdgeButton` stroke at the bottom edge would echo it
  without drowning the content area.
- **Android Makers**: pixel-art Eiffel Tower on their hero. Could
  surface as a 1 dp outlined "pixel" border on the session-card
  container under heavy inspiration, but even this risks looking cute
  rather than expressive.
- **Droidcon**: the green is the flourish. Nothing else needed.
- **DevFest**: Google's organic squiggles. The celebration icon and
  Google Sans Flex typography carry the right Google-event feel; adding
  an illustrated squiggle to a 192-pixel circle would be clutter.

## 5. Screen architecture

Every top-level screen in Wear follows the same shape:

```kotlin
ScreenScaffold(
    scrollState = listState,
    scrollIndicator = {
        if (!LocalScrollCaptureInProgress.current) ScrollIndicator(listState)
    },
    edgeButton = { EdgeButton(…) { Icon(…) } },
) { contentPadding ->
    TransformingLazyColumn(
        state = listState,
        contentPadding = contentPadding,   // non-negotiable — see WEAR_UI.md §1
    ) {
        item { ScreenHeader(…) }
        sectionA(…)
        sectionB(…)
    }
}
```

- **`AppScaffold`** lives one level up in [`ConfettiApp.kt`](../wearApp/src/main/java/dev/johnoreilly/confetti/wear/ui/ConfettiApp.kt)
  and owns the `TimeText`. Individual screens don't wrap themselves in
  `AppScaffold`.
- **`ScreenScaffold`'s `contentPadding` lambda MUST be used.** Dropping it
  causes the top item to slide under the `TimeText` on round faces.
- **The last item uses `Modifier.minimumVerticalContentPadding(...)`** from
  `TransformingLazyColumnItemScope` to clear the EdgeButton on round faces
  — see `CardDefaults.minimumVerticalListContentPadding` and
  `ButtonDefaults.minimumVerticalListContentPadding` for the right default
  per item type.
- **`LocalScrollCaptureInProgress`** — always wrap `ScrollIndicator` in the
  guard shown above. Without it, `@ScrollingPreview(mode = LONG)` stitched
  captures include a ghost scroll indicator at random slice opacities.

### Component choices

| Component | When | Notes |
|---|---|---|
| `TitleCard` | Session rows, list items with a primary + secondary label | Default `CardDefaults` colors are correct |
| `Button` (filled) | Primary destructive/navigation actions | Change conference, sign out |
| `Button` + `filledVariant` | Conference-picker chips | Tints with the seedColor via `ButtonDefaults.filledVariantButtonColors()` |
| `EdgeButton` (small) | Secondary screen-level action (settings, refresh) | Hugs the bottom edge, revealed only when list is scrolled to its end |
| `SwitchButton` | Binary prefs (LTE allowed) | Label + icon slots |
| `ListHeader` | Top-of-screen title | Keep text centred, titleMedium |
| `ListSubHeader` | Inline section break | Our `SectionHeader` wraps this — labelMedium in `primary` |

## 6. Motion

- **M3 Expressive morphing** comes from `TransformingLazyColumn` itself —
  `rememberTransformationSpec()` + `SurfaceTransformation` on items. We
  take the defaults today; no custom specs.
- **Previews pin motion off.** `@ScrollingPreview(mode = LONG,
  reduceMotion = true)` (default) wraps the body in
  `LocalReduceMotion provides ReduceMotion(true)` so slice-to-slice
  transform state stays stable. Don't flip `reduceMotion = false` in a
  preview — the stitched PNG will look ragged.
- **Animations inside a composable** (spinners, `withFrameNanos` loops) are
  captured deterministically by the preview renderer, which pauses the
  `mainClock` and advances by `CAPTURE_ADVANCE_MS` — we don't need to
  hand-write `awaitIdle`.

## 7. Mobile parity

Today the Android phone app ships a fixed Material 3 color scheme
(Purple/Orange/Blue, see [`androidApp/src/main/.../ui/Color.kt`](../androidApp/src/main/java/dev/johnoreilly/confetti/ui/Color.kt)
and [`Theme.kt`](../androidApp/src/main/java/dev/johnoreilly/confetti/ui/Theme.kt)),
optionally promoted to Android 12+ dynamic color. Typography is the
Material 3 default scale with no custom fonts.

The Wear treatment — seedColor-per-conference plus a fixed Expressive
typography — is deliberately compatible with that surface:

- `ConfettiTheme` on mobile could adopt the same seedColor input with no
  behaviour change to the current purple/orange/blue preset (pass
  `seedColor = null` and the defaults win).
- The same Roboto Flex + Inter pair can be wired into mobile with a
  duplicated `Fonts.kt` (or a shared common module) once we want it.

We don't do that migration in this PR — this guide just makes the shape
explicit so the mobile work is a project plan, not an invention.

## 8. Checklist when adding a new screen

- [ ] Uses `ScreenScaffold` with `contentPadding` passed into
      `TransformingLazyColumn`.
- [ ] `ScrollIndicator` is guarded by `!LocalScrollCaptureInProgress.current`.
- [ ] Every `Text` states its `style` and `color` via `MaterialTheme`
      tokens — no raw `sp`, no raw `Color(0xFF…)`.
- [ ] Last item uses `Modifier.minimumVerticalContentPadding(...)` so an
      EdgeButton doesn't crop it on round faces.
- [ ] Has a regular `@WearPreviewLargeRound` preview **and** a
      `@ScrollingPreview(mode = LONG)` preview if the list can exceed the
      viewport.
- [ ] Rendered locally with `compose-preview show` before review; the
      preview diff posted in the PR comment.
