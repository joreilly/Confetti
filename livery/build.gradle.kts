// Confetti's server-driven UI (Livery: Remote Compose templates per conference). The app depends
// on this module only in the sduiDebug build type (androidApp: sduiDebugImplementation), so every
// other variant ships without Livery; the module itself builds, tests and previews in its own
// debug and release like any library. See livery/README.md.
plugins {
    id("com.android.library")
    alias(libs.plugins.compose.compiler)
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
    implementation(libs.livery.compose)

    coreLibraryDesugaring(libs.desugar)
}
