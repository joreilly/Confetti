@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("io.github.takahirom.roborazzi")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.screenshot)
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.kapt")
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
    compileSdk = AndroidSdk.compile
    defaultConfig {
        applicationId = "dev.johnoreilly.confetti"
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target

        versionCode = versionCode()
        versionName = versionName()

        resourceConfigurations += listOf("en", "fr")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("appfunctions:aggregateAppFunctions", "true")
            arg("appfunctions:generateMetadataFromSchema", "true")
        }
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
//        create("benchmark") {
//            isShrinkResources = true
//            isMinifyEnabled = true
//            signingConfig = signingConfigs.getByName("confetti")
//            setProguardFiles(
//                listOf(
//                    getDefaultProguardFile("proguard-android.txt"),
//                    "proguard-benchmark.pro"
//                )
//            )
//            matchingFallbacks.addAll(listOf("release"))
//        }
        getByName("debug") {
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
            optIn("androidx.compose.material.ExperimentalMaterialApi")
            optIn("kotlin.RequiresOptIn")
        }
    }
}

dependencies {
    implementation(projects.common.car)
    implementation(projects.shared)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.coil.compose)

    implementation(libs.decompose.decompose)
    implementation(libs.decompose.extensions.compose)

    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.material3.core)
    implementation(libs.splash.screen)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.horologist.datalayer)
    implementation(libs.horologist.datalayer.phone)

    val excludeCrashlyticsConflicts = Action<ExternalModuleDependency> {
        // Crashlytics and PerfMon depend on datastore v1.0 but we're using v1.1
        exclude(group = "androidx.datastore", module = "datastore-preferences")
        // https://github.com/firebase/firebase-android-sdk/issues/5997
        exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    }
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics, excludeCrashlyticsConflicts)
    implementation(libs.firebase.analytics, excludeCrashlyticsConflicts)
    implementation(libs.firebase.performance, excludeCrashlyticsConflicts)
    implementation(libs.play.services.auth)
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.googleid)
    implementation(libs.androidx.credentials.play.services.auth)

    implementation(libs.androidx.appfunctions)
    implementation(libs.androidx.appfunctions.service)
    ksp(libs.androidx.appfunctions.compiler)
    implementation(libs.androidx.appsearch)
    implementation(libs.androidx.appsearch.ktx)
    implementation(libs.androidx.appsearch.platform.storage)
    implementation(libs.kotlinx.coroutines.guava)
    kapt(libs.androidx.appsearch.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.koin.test)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    testImplementation(libs.coil.test)
    debugImplementation(libs.compose.ui.manifest)

    screenshotTestImplementation(libs.androidx.compose.ui.tooling)

    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("androidx.test.ext:truth:1.6.0")
}
