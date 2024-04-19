plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.compose")
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
}

dependencies {
    implementation(libs.junit)
    implementation(libs.robolectric)
    implementation(libs.compose.ui.test.junit4)
    implementation(libs.koin.test)
    implementation(libs.okio)

    coreLibraryDesugaring(libs.desugar)
}
