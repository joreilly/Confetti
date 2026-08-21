@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            google {
                content {
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                }
            }
            mavenCentral()
            maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
            exclusiveContent {
                forRepository { it.maven("https://storage.googleapis.com/apollo-snapshots/m2") }
                filter {
                    includeVersionByRegex("com.apollographql.execution", ".*", ".*SNAPSHOT.*")
                }
            }
        }
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                // Appengine plugin doesn't publish the marker
                "com.google.cloud.tools.appengine" -> useModule("com.google.cloud.tools:appengine-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "Confetti"

// BuildFetch remote Gradle build cache. Complements the local build cache (org.gradle.caching=true
// in gradle.properties) by sharing task outputs across CI runs and developer machines.
//
// Auth: the token is resolved from the first non-blank of, in order:
//   1. env  BUILDFETCH_CONFETTI_GRADLE_REMOTE_CACHE_TOKEN   (project-specific)
//   2. prop BUILDFETCH_CONFETTI_GRADLE_REMOTE_CACHE_TOKEN
//   3. env  BUILDFETCH_GRADLE_REMOTE_CACHE_TOKEN            (shared / general fallback)
//   4. prop BUILDFETCH_GRADLE_REMOTE_CACHE_TOKEN
// The project-specific name lets a developer hold a separate readonly token per BuildFetch project
// in a single ~/.gradle/gradle.properties (sibling repos point at different caches, so one shared
// token can't authenticate them all). The general name stays as a fallback — it's what CI exports
// and what a single-project setup can use. Env wins over a gradle property of the same name so CI
// overrides a stray local property.
//
// The token is treated as absent unless it is non-blank. CI declares the env var unconditionally
// (`BUILDFETCH_GRADLE_REMOTE_CACHE_TOKEN: ${{ secrets.… }}`), so an unprovisioned secret or a fork
// PR exports it as an empty string — which Gradle's `environmentVariable(...)` still reports as
// *present*. Filtering each source for blanks independently preserves the intended no-op (empty ⇒
// cache disabled) and lets a later source take over even when an earlier one is present-but-empty.
// When nothing resolves the cache disables itself (isEnabled below), so fork PRs and un-provisioned
// checkouts fall back to the local cache with no error.
//
// Push: writes are restricted to trusted CI builds. CI sets ON_CI=true only on main-branch runs, so
// PRs and developer machines are read-only. The gate is value-based (not env-var presence) so an
// explicit ON_CI=false is honoured as read-only.
val onCi = providers.environmentVariable("ON_CI").orElse("false").get().toBoolean()

// Non-blank view of a single env var / gradle property: trims and drops empties so a
// present-but-empty source doesn't shadow a later fallback (see the header comment).
val nonBlank = { source: Provider<String> -> source.map { it.trim() }.filter { it.isNotEmpty() } }
val cacheToken =
    nonBlank(providers.environmentVariable("BUILDFETCH_CONFETTI_GRADLE_REMOTE_CACHE_TOKEN"))
        .orElse(nonBlank(providers.gradleProperty("BUILDFETCH_CONFETTI_GRADLE_REMOTE_CACHE_TOKEN")))
        .orElse(nonBlank(providers.environmentVariable("BUILDFETCH_GRADLE_REMOTE_CACHE_TOKEN")))
        .orElse(nonBlank(providers.gradleProperty("BUILDFETCH_GRADLE_REMOTE_CACHE_TOKEN")))
        .orNull

// True only when this run will actually push to the remote: a trusted main-branch run (ON_CI)
// with a usable token. Anything else (PRs, dev machines, or a main run whose token is
// unprovisioned/blank) does not push, so it must keep the local cache.
val remotePushEnabled = onCi && cacheToken != null

buildCache {
    // On the trusted main-branch runs, CI is the sole writer of the BuildFetch remote cache —
    // and the only thing that populates it for every other consumer (PRs, developer machines).
    // Gradle never re-uploads a *local* build-cache hit to the remote; it pushes to the remote
    // only when a task actually executes. setup-gradle restores a warm local build cache
    // (caches/build-cache-1) from the GitHub Actions cache, so with it in place every task
    // resolves as FROM-CACHE (local), nothing is pushed, and the remote stays empty (dev
    // machines then see 0 remote hits). Disabling the local cache on the pushing runs forces
    // tasks to execute-and-push, or to hit the remote directly, so BuildFetch actually gets
    // seeded.
    //
    // Gate this on remotePushEnabled, not just ON_CI: if the token is unprovisioned/blank the
    // remote below disables itself, and disabling the local cache too would make that main run
    // execute every cacheable task with *no* cache at all. Off-CI, on PRs, and on token-less
    // main runs the local cache stays on.
    local {
        isEnabled = !remotePushEnabled
    }
    remote<HttpBuildCache> {
        url = uri("https://cache.eu-central-a.buildfetch.com/xTXBBS/gradle/")

        credentials {
            username = "token-auth"
            password = cacheToken
        }

        isPush = onCi

        isEnabled = cacheToken != null
    }
}

include(":androidApp")
//include(":androidBenchmark")
//include(":automotiveApp")
include(":common:car")
include(":shared")
include(":backend")
include(":backend:service-graphql")
include(":backend:datastore")
include(":backend:service-import")
include(":backend:terraform")
include(":landing-page")
include(":wearApp")
//include(":wearBenchmark")
include(":compose-desktop")
include(":compose-web")
include(":proto")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    "This project needs to be run with Java 17 or higher (found: ${JavaVersion.current()})."
}

includeBuild("build-logic")
