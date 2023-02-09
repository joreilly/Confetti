plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.apollographql.apollo3")
    id("com.google.devtools.ksp")
    id("com.rickclephas.kmp.nativecoroutines")
    id("co.touchlab.faktory.kmmbridge")
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

                api(libs.bundles.multiplatform.settings)
                api(libs.koin.core)

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
                implementation(libs.okhttp)
                implementation(libs.okhttp.coroutines)
                implementation(libs.okhttp.logging.interceptor)
                implementation(libs.coil.base)
                implementation(libs.koin.android)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by getting {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }

        val mobileMain by getting {
            dependencies {
                implementation(libs.kmm.viewmodel)
            }
        }

        val jvmMain by getting {
            dependencies {
                // hack to allow use of MainScope() in shared code used by JVM console app
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.okhttp)
                implementation(libs.okhttp.coroutines)
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
    }
    namespace = "dev.johnoreilly.confetti.shared"
}

apollo {
    service("service") {
        packageName.set("dev.johnoreilly.confetti")
        codegenModels.set("operationBased")
        generateSchema.set(true)
        mapScalar(
            "Instant",
            "kotlinx.datetime.Instant",
            "com.apollographql.apollo3.adapter.KotlinxInstantAdapter"
        )

        mapScalar(
            "LocalDate",
            "kotlinx.datetime.LocalDate",
            "com.apollographql.apollo3.adapter.KotlinxLocalDateAdapter"
        )

        introspection {
            endpointUrl.set("https://graphql-dot-confetti-349319.uw.r.appspot.com/graphql")
            //endpointUrl.set("http://localhost:8080/graphql")
            schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
        }
        val apolloKey = System.getenv("APOLLO_KEY")
        if (apolloKey.isNullOrBlank().not()) {
            registry {
                key.set(apolloKey)
                graph.set("Confetti")
                schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}

kmmbridge {
    frameworkName.set("ConfettiKit")
    githubReleaseArtifacts()
    githubReleaseVersions()
    spm()
    versionPrefix.set("0.7")
}

allprojects {
    afterEvaluate {
        // temp fix until sqllight includes https://github.com/cashapp/sqldelight/pull/3671
        project.extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
            ?.let { kmpExt ->
                kmpExt.targets
                    .filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
                    .flatMap { it.binaries }
                    .forEach { it.linkerOpts("-lsqlite3") }
            }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}