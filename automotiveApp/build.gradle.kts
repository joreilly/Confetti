@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.compose.compiler)
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
    compileSdk = AutomotiveSdk.compile
    defaultConfig {
        applicationId = "dev.johnoreilly.confetti"
        minSdk = AutomotiveSdk.min
        targetSdk = AutomotiveSdk.target

        versionCode = versionCode()
        versionName = versionName()

        resourceConfigurations += listOf("en", "fr")
    }

    signingConfigs {
        create("confetti") {
            keyAlias = "confetti"
            keyPassword = "confetti"
            storeFile = file("confetti.keystore")
            storePassword = "confetti"
        }
        create("release") {
            (keystoreProperties["keyPath"] as String?)?.let {
                storeFile = file(it)
            }
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storePassword = keystoreProperties["storePassword"] as String?
            enableV2Signing = true
        }
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

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            )
        }
        create("githubRelease") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("confetti")
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            )

            matchingFallbacks += listOf("release")
        }
        create("benchmark") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("confetti")
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-benchmark.pro"
                )
            )
            matchingFallbacks.addAll(listOf("release"))
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("confetti")
        }
    }
    namespace = "dev.johnoreilly.confetti"
}

dependencies {

    implementation(project(":common:car"))
    implementation(project(":shared"))

    implementation(libs.lifecycle.runtime.compose)

    val excludeAndroidxDataStore = Action<ExternalModuleDependency> {
        // Crashlytics and PerfMon depend on datastore v1.0 but we're using v1.1
        exclude(group = "androidx.datastore", module = "datastore-preferences")
    }
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics, excludeAndroidxDataStore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.performance, excludeAndroidxDataStore)
    implementation(libs.play.services.auth)

    coreLibraryDesugaring(libs.desugar)

    implementation(libs.car.app.automotive)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
}