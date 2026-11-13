# Sessions list benchmark

A fling down the sessions list on the desktop JVM, timed frame by frame. It compares Confetti's own
`SessionItemView` (Compose UI, Material 3) with the same sessions drawn by each series' Livery
session template.

```sh
git fetch origin livery-templates
mkdir -p /tmp/livery-catalogs && git archive origin/livery-templates catalogs | tar -x -C /tmp/livery-catalogs
./gradlew :livery:benchmark:sessionsBenchmark -Pconfetti.benchmark.catalogs=/tmp/livery-catalogs/catalogs
```

The report goes to `livery/benchmark/build/benchmark/sessions.md`.

## What it measures

The harness keeps a window of five rows on screen, and each frame scrolls on two rows that compose
for the first time while two leave. Each frame's wall time covers composition, layout and a software
draw of the window, so it is what rows arriving cost. This follows rc-players' `RcFeedProfile`.

It is deliberately not a `LazyColumn`: prefetch would hide the per-row cost, and that cost is
exactly what prefetch has to fit into idle time on a device. Every lane is warmed first, then the
lanes interleave over five rounds.

| Lane | What it is |
| --- | --- |
| Confetti SessionItemView | The native row: `ListItem`, `FlowRow` chips, bookmark button. |
| Livery, a host per card | Each row gets its own `LiveryHost`, and so its own `LiveryResources`. This is how `:livery:android`'s slots were built before `SeriesCatalog.resources`: every row decodes its template and reads its fonts again. |
| Livery, one warmed host per series | One shared `LiveryResources`, with the template prepared and its fonts read before the list shows. This is what `SeriesCatalog.warm` does on every catalog revision. |

*Rows drawn* is the share of rows that drew before they scrolled away. A Livery row draws nothing
until its fonts (and, from Livery's background preparation onward, its document) are in memory. So
a lane can only be fast honestly if it also draws its rows.

## On a device

This is a JVM proxy: software raster, a desktop JIT, no `RenderThread`. On a device, measure the
same fling with Macrobenchmark's `FrameTimingMetric` on a non-debuggable build of each arm.

Livery is only in `sduiDebug` today, so that needs a release-like sdui build type first.
