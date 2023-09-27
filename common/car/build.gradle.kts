@file:Suppress("UnstableApiUsage")

import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
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
        buildConfig = true
    }

    namespace = "dev.johnoreilly.confetti.car"
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.coil.compose)

    implementation(libs.car.app.auto)

    implementation(libs.play.services.auth)

    coreLibraryDesugaring(libs.desugar)
}