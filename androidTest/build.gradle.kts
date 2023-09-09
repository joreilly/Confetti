import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    namespace = "dev.johnoreilly.confetti"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(libs.snapshot.android)

    implementation(libs.junit)
    implementation(libs.robolectric)
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.koin.test)
    implementation(libs.okio)

    coreLibraryDesugaring(libs.desugar)
}
