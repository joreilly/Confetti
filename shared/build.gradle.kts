import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.apollographql.apollo3")
    id("org.jetbrains.compose") version libs.versions.compose.multiplatform
    id("com.google.devtools.ksp")
    id("co.touchlab.faktory.kmmbridge")
    id("com.squareup.wire")
    id("kotlin-parcelize")
    id("maven-publish")
}

configureCompilerOptions()

version = "1.0"

dependencies {
    implementation(platform(libs.firebase.bom))
}

wire {
    kotlin {
    }
}

kotlin {
    androidTarget()
    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ConfettiKit"
            isStatic = true

            export(libs.decompose.decompose)
            export(libs.essenty.lifecycle)
        }
    }

    macosArm64("macos") {
        binaries.framework {
            baseName = "ConfettiKit"
            isStatic = true

            export(libs.decompose.decompose)
            export(libs.essenty.lifecycle)
        }
    }

    targetHierarchy.default {
        common {
            group("mobile") {
                withIos()
                withAndroidTarget()
            }
        }
    }

    sourceSets {

        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.atomicfu)
                api(libs.kotlinx.datetime)

                api(libs.bundles.multiplatform.settings)
                api(libs.koin.core)
                implementation(libs.koin.compose.multiplatform)

                api(libs.apollo.runtime)
                api(libs.bundles.apollo)

                api(libs.decompose.decompose)
                api(libs.essenty.lifecycle)

                // Multiplatform Logging
                api(libs.napier)

                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(libs.image.loader)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val mobileMain by getting {
            dependencies {
                implementation(libs.firebase.mpp.auth)
            }
        }

        val iosMain by getting {
            dependsOn(mobileMain)
        }

        val androidMain by getting {
            dependsOn(mobileMain)
            dependencies {
                api(libs.androidx.lifecycle.viewmodel.ktx)
                implementation(libs.okhttp)
                implementation(libs.okhttp.coroutines)
                implementation(libs.okhttp.logging.interceptor)
                api(libs.coil.base)
                api(libs.koin.android)
                api(libs.koin.workmanager)
                api(libs.okio)
                implementation(libs.horologist.datalayer)

                implementation(libs.google.services)
                implementation(libs.firebase.analytics)
                implementation(libs.compose.navigation)

                api(libs.androidx.work.runtime.ktx)

                api(libs.multiplatform.settings.datastore)
                api(libs.androidx.datastore)
                api(libs.androidx.datastore.preferences)
            }
        }

        val jvmMain by getting {
            dependencies {
                // hack to allow use of MainScope() in shared code used by JVM console app
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.okhttp)
                implementation(libs.okhttp.coroutines)
                implementation(libs.apollo.testing)
            }
        }

        val macosMain by getting {
        }

    }


}

android {
    compileSdk = AndroidSdk.compile

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
    }

    buildFeatures {
        buildConfig = true
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
        generateDataBuilders.set(true)
        generateFragmentImplementations.set(true)
        generateSchema.set(true)
        mapScalar(
            "LocalDateTime",
            "kotlinx.datetime.LocalDateTime",
            "com.apollographql.apollo3.adapter.KotlinxLocalDateTimeAdapter"
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
    mavenPublishArtifacts()
    githubReleaseVersions()
    spm()
    versionPrefix.set("0.8")
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}

tasks.create("runJvmMain", JavaExec::class.java) {
    val jars = files().apply {
        from(configurations.getByName("jvmRuntimeClasspath"))
        from(tasks.named("jvmJar"))
    }
    this.setClasspath(jars)
    this.mainClass.set("dev.johnoreilly.confetti.MainKt")
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.repsy.io/mvn/joreilly/confetti")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

compose {
    kotlinCompilerPlugin.set(libs.versions.compose.compiler.get())
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=1.9.10")
}
