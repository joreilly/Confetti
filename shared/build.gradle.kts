@file:Suppress("OPT_IN_USAGE")

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.apollographql.apollo3")
    id("com.google.devtools.ksp")
    id("com.rickclephas.kmp.nativecoroutines")
}

version = "1.0"

kotlin {
    android()
    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ConfettiKit"
        }
    }


    targetHierarchy.default {
        common {
            group("mobile") {
                withIos()
                withAndroid()
            }
        }
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)

                api(libs.apollo.runtime)
                implementation(libs.bundles.apollo)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.lifecycle.viewmodel.ktx)
            }
        }

        val mobileMain by getting {
            dependencies {
                implementation(libs.kmm.viewmodel)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

android {
    compileSdk = AndroidSdk.compile
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "dev.johnoreilly.confetti.shared"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}

apollo {
    service("service") {
        packageName.set("dev.johnoreilly.confetti")
        mapScalar(
            "LocalDateTime",
            "kotlinx.datetime.LocalDateTime",
            "com.apollographql.apollo3.adapter.KotlinxLocalDateTimeAdapter"
        )
        introspection {
            endpointUrl.set("http://localhost:8080/graphql")
            schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
        }
    }
}

tasks.create("runJvmMain", JavaExec::class.java) {
    val jars = files().apply {
        from(configurations.getByName("jvmRuntimeClasspath"))
        from(tasks.named("jvmJar"))
    }
    this.setClasspath(jars)
    this.mainClass.set("dev.johnoreilly.confetti.MainKt")
}
