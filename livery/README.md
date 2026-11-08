# :livery

Confetti's server-driven UI: each conference's [Livery](https://github.com/yschimke/livery)
templates (Remote Compose documents) drawn in place of Confetti's own composables, with those
composables as the fallback.

- Only the `sduiDebug` build type of `:androidApp` depends on this module
  (`sduiDebugImplementation(projects.livery)`). `debug`, `release` and the other variants don't
  include it, and draw Confetti's own UI.
- The module builds, tests and renders its previews in its own `debug` and `release`, like any
  library, so CI covers it without the app variant.
