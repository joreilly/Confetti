// The contracts between Confetti and its Livery designs (see Contracts.kt), shared by the
// authoring side (:livery-catalog, which generates catalog/contracts/*.json from them) and every
// app that fills a slot (:shared, so Android, iOS, desktop and web build their data against the
// same declarations).
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

configureCompilerOptions()

kotlin {
    jvm()
    android {
        namespace = "dev.johnoreilly.confetti.livery.contracts"
        compileSdk = AndroidSdk.compile
        minSdk = AndroidSdk.min
    }
    iosArm64()
    iosSimulatorArm64()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            // api, with its BOM: consumers (shared, and through it every app) need the version too.
            api(project.dependencies.platform(libs.kotlinx.serialization.bom))
            api(libs.kotlinx.serialization)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
