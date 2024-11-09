@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlinx-serialization")
    id("io.github.takahirom.roborazzi")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.screenshot)
}

configureCompilerOptions()

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

val versionMajor = 1
val versionMinor = 0

val versionNum: String? by project

fun versionCode(): Int {
    versionNum?.let {
        val code: Int = (versionMajor * 1000000) + (versionMinor * 1000) + it.toInt()
        println("versionCode is set to $code")
        return code
    } ?: return 1
}

fun versionName(): String {
    versionNum?.let {
        val name = "${versionMajor}.${versionMinor}.${versionNum}"
        println("versionName is set to $name")
        return name
    } ?: return "1.0"
}

android {
    compileSdk = WearSdk.compile
    defaultConfig {
        applicationId = "dev.johnoreilly.confetti"
        minSdk = WearSdk.min
        targetSdk = WearSdk.target

        versionCode = 1
        versionName = "1.0"
        versionCode = versionCode()
        versionName = versionName()
    }

    signingConfigs {
        create("confetti") {
            keyAlias = "confetti"
            keyPassword = "confetti"
            storeFile = file("confetti.keystore")
            storePassword = "confetti"
        }
        create("release") {
            storeFile = file("/Users/joreilly/dev/keystore/galwaybus_android.jks")
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storePassword = keystoreProperties["storePassword"] as String?
            enableV2Signing = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    kotlinOptions {
        freeCompilerArgs += "-opt-in=com.google.android.horologist.annotations.ExperimentalHorologistApi"
        freeCompilerArgs += "-opt-in=androidx.wear.compose.material.ExperimentalWearMaterialApi"
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
        }
        create("benchmark") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("confetti")
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-benchmark.pro"))
            matchingFallbacks.addAll(listOf("release"))
        }
        create("githubRelease") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("confetti")
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))

            matchingFallbacks += listOf("release")
        }
        getByName("debug") {
            isShrinkResources = false
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("confetti")
        }
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/AL2.0",
                "/META-INF/LGPL2.1",
                "/META-INF/versions/**"
            )
        }
    }

    namespace = "dev.johnoreilly.confetti"

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

kotlin {
    sourceSets.all {
        languageSettings {
            optIn("kotlin.RequiresOptIn")
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.splash.screen)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)
    implementation(libs.okio)
    implementation(libs.wear.compose.material3)
    implementation(libs.androidx.protolayout.material3)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.workmanager)

    implementation(libs.kmm.viewmodel)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.koin.android)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.wear.compose.material)
    implementation(libs.horologist.compose.layout)
    api(libs.horologist.compose.material)
    api(libs.horologist.composables)
    implementation(libs.horologist.tiles)
    implementation(libs.wear.complications.data)

    implementation(libs.androidx.protolayout.proto)
    implementation(libs.androidx.protolayout.expression)
    implementation(libs.androidx.protolayout.expression.pipeline)

    implementation(libs.horologist.auth.composables)
    implementation(libs.horologist.auth.ui)
    implementation(libs.horologist.auth.data)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.play.services.auth)

    implementation(libs.horologist.datalayer)
    implementation(libs.horologist.datalayer.watch)
    implementation(libs.horologist.networkawareness.core)
    implementation(libs.horologist.networkawareness.db)
    implementation(libs.horologist.networkawareness.okhttp)
    implementation(libs.horologist.networkawareness.ui)
    api(libs.horologist.images.coil)

    implementation(libs.material3.core)

    implementation(libs.decompose.decompose)
    implementation(libs.decompose.extensions.compose)

    val excludeCrashlyticsConflicts = Action<ExternalModuleDependency> {
        // Crashlytics and PerfMon depend on datastore v1.0 but we're using v1.1
        exclude(group = "androidx.datastore", module = "datastore-preferences")
        // https://github.com/firebase/firebase-android-sdk/issues/5997
        exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    }
    implementation(libs.firebase.crashlytics, excludeCrashlyticsConflicts)
    implementation(libs.firebase.analytics, excludeCrashlyticsConflicts)
    implementation(libs.firebase.performance, excludeCrashlyticsConflicts)
    implementation(libs.firebase.auth, excludeCrashlyticsConflicts)

    implementation(libs.decompose.decompose)
    implementation(libs.decompose.extensions.compose)
    implementation(libs.room.runtime)
    api(libs.coil.base)

    implementation(libs.androidx.protolayout.material)

    coreLibraryDesugaring(libs.desugar)

    debugImplementation(libs.compose.ui.manifest)
    debugImplementation(libs.androidx.complications.rendering)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.tiles.tooling.preview)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.accompanist.testharness)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.complications.rendering)
    testImplementation(libs.horologist.compose.tools)
    testImplementation(libs.horologist.roboscreenshots)
    testImplementation(libs.horologist.images.coil)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    implementation(libs.coil.test)
    implementation(kotlin("test"))

    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register<PlayStoreScreenshotTask>("generateImages") {
    selectedImages.from("snapshot/ConferenceScreenTest/conferencesScreen[GenericLargeRound].png")
    selectedImages.from("snapshot/SessionsScreenTest/sessionsScreen[GenericLargeRound].png")
    selectedImages.from("snapshot/SessionsDetailsTest/sessionDetailsScreen[GenericLargeRound].png")
    selectedImages.from("snapshot/SpeakerDetailsTest/speakerDetailsScreen[GenericLargeRound].png")
    selectedImages.from("snapshot/TileScreenshotTest/tile[GenericLargeRound].png")
    selectedImages.from("snapshot/ConferenceHomeScreenTest/conferenceHomeScreen[GenericLargeRound].png")
    output.set(file("../fastlane/metadata/android/en-US/images/wearScreenshots"))
}

tasks.register<ReadmeScreenshotTask>("readmeScreenshot") {
    selectedImages.from("snapshot/ConferenceScreenTest/conferencesScreen[GenericLargeRound].png")
    selectedImages.from("snapshot/SessionsScreenTest/sessionsScreen[GenericLargeRound].png")
    selectedImages.from("snapshot/SessionsDetailsTest/sessionDetailsScreen[GenericLargeRound].png")
    selectedImages.from("snapshot/SpeakerDetailsTest/speakerDetailsScreen[GenericLargeRound].png")
    output.set(file("images/wearScreenshots.png"))
}
