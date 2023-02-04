@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.util.*


plugins {
    id("com.android.application")
    kotlin("android")
    if (File("wearApp/google-services.json").exists()) {
        id("com.google.gms.google-services")
        id("com.google.firebase.crashlytics")
    }
}

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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
        )
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
            matchingFallbacks.addAll(listOf("release", "debug"))
        }
        create("githubRelease") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("confetti")
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro",
                ),
            )
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))

            matchingFallbacks += listOf("release")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("confetti")
        }
    }
    namespace = "dev.johnoreilly.confetti"
}


kotlin {
    sourceSets.all {
        languageSettings {
            optIn("androidx.compose.material.ExperimentalMaterialApi")
            optIn("kotlin.RequiresOptIn")
        }
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.coil.compose)
    implementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.splash.screen)
    implementation(libs.compose.navigation)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.kmm.viewmodel)

    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.navigation)
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.composables)
    implementation(libs.horologist.base.ui)
    implementation(libs.horologist.tiles)
    implementation(libs.wear.complications.data)

    implementation(libs.google.services)
    implementation(libs.firebase.crashlytics)
    if (file("google-services.json").exists()) {
        implementation(libs.firebase.analytics)
        implementation(libs.firebase.performance)
    }
}
