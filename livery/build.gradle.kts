// Confetti's server-driven UI (Livery: Remote Compose templates per conference). The app depends
// on this module only in the sduiDebug build type (androidApp: sduiDebugImplementation), so every
// other variant ships without Livery; the module itself builds, tests and previews in its own
// debug and release like any library. See livery/README.md.
plugins {
    id("com.android.library")
    alias(libs.plugins.compose.compiler)
    // Livery's catalog builder, for its BuildLiveryCatalog task type only: the plugin itself bundles
    // one catalog, and Confetti bundles one per series (below).
    alias(libs.plugins.livery.catalog) apply false
}

configureCompilerOptions()

android {
    compileSdk = AndroidSdk.compile
    defaultConfig {
        minSdk = AndroidSdk.min
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }

    namespace = "dev.johnoreilly.confetti.livery"
}

dependencies {
    implementation(enforcedPlatform(libs.compose.bom))
    implementation(projects.shared)
    implementation(libs.livery.client)
    implementation(libs.livery.compose) {
        // rc-player-compose declares Ktor's CIO engine but never uses it; beside OkHttp it would
        // make HttpClient()'s default engine arbitrary for the app's own clients too.
        exclude(group = "io.ktor", module = "ktor-client-cio")
    }
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3.core)
    implementation(libs.kotlinx.datetime)
    // Livery's catalog and image fetches on the app's own OkHttpClient (ConfettiLivery.kt).
    implementation(libs.okhttp)
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.okhttp)
    // The sdui experiment's arm per conference (Firebase A/B Testing over Remote Config).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test.junit4)

    coreLibraryDesugaring(libs.desugar)
}

// Each catalog under livery-catalog/catalogs/<series> (confetti, the base, and any conference's),
// built and checked like `livery build --inspect` into this module's assets at livery/<series>/,
// where AndroidBundled.fromAssets(context, "livery/<series>") reads it back. The bundled copy has
// serial 0, so a catalog the app downloads always replaces it.
val liveryCatalogs = rootProject.layout.projectDirectory.dir("livery-catalog/catalogs").asFile
    .listFiles { f -> f.isDirectory }.orEmpty().map { it.name }.sorted()
val liveryCatalogTasks = liveryCatalogs.map { series ->
    tasks.register<ee.schimke.livery.gradle.BuildLiveryCatalog>("buildLiveryCatalog${series.replaceFirstChar { it.uppercase() }}") {
        group = "build"
        description = "Builds the $series Livery catalog into generated assets"
        source.set(rootProject.layout.projectDirectory.dir("livery-catalog/catalogs/$series"))
        assetsPath.set("livery/$series")
        inspect.set(true)
        outputDir.set(layout.buildDirectory.dir("generated/livery/$series"))
    }
}

androidComponents {
    onVariants { variant ->
        liveryCatalogTasks.forEach { variant.sources.assets?.addGeneratedSourceDirectory(it, ee.schimke.livery.gradle.BuildLiveryCatalog::outputDir) }
    }
}
