# Livery in Confetti

Server-driven conference designs, built with [Livery](https://github.com/yschimke/livery):

| Module | Directory | What it is |
| --- | --- | --- |
| `:livery:contracts` | [`contracts/`](contracts/) | The contracts between Confetti and its designs (KMP), and the data the app fills them with |
| `:livery:catalog` | [`catalog/`](catalog/) | The designs, as Kotlin `@RemoteComposable` functions, and the build that captures them into each series' catalog. See its [README](catalog/README.md) |
| `:livery:wear-designs` | [`wear-designs/`](wear-designs/) | Each series' Wear variants of its designs (`variant` wear, tagged `device=wear`), written with Wear's Remote Material 3 (remote-m3-catalog's multiplatform port) and captured on the plain JVM; `:livery:catalog:assembleLiveryCatalogs` adds them to each series' catalog |
| `:livery:android` | [`android/`](android/) | The Android library the app's `sduiDebug` variant uses: the slots, the experiment arms, the bundled catalogs. See its [README](android/README.md) |
| `:livery:benchmark` | [`benchmark/`](benchmark/) | A JVM fling of the sessions list: Confetti's `SessionItemView` against the series' Livery session cards. See its [README](benchmark/README.md) |

The source is always Kotlin: catalogs, `.rc` documents and renders are build output, or live on
the `livery-templates` branch.
