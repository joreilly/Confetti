plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("com.google.devtools.ksp")
    id("com.squareup.wire")
}

wire {
    kotlin {
    }
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "dev.johnoreilly.confetti.proto"
        compileSdk = AndroidSdk.compile
        minSdk = AndroidSdk.min
    }
    jvm()

    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.androidx.datastore)
            }
        }
    }
}
