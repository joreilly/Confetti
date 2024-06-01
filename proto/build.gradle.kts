plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("com.squareup.wire")
}

wire {
    kotlin {
    }
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm()

    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.androidx.datastore)
            }
        }
    }
}

android {
    compileSdk = AndroidSdk.compile
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = AndroidSdk.min
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    namespace = "dev.johnoreilly.confetti.proto"
}


dependencies {
    coreLibraryDesugaring(libs.desugar)
}
