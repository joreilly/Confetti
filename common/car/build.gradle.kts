@file:Suppress("UnstableApiUsage")

import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.gms.google-services")
}

configureCompilerOptions()

android {
    compileSdk = AndroidSdk.compile
    defaultConfig {
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    namespace = "dev.johnoreilly.confetti"
}

dependencies {

    implementation(project(":shared"))

    implementation(libs.coil.compose)

    implementation(libs.car.app.auto)

    implementation(libs.play.services.auth)
}