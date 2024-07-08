@file:Suppress("OPT_IN_USAGE")
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.apollographql.apollo")
    id("org.jetbrains.compose")
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
    id("maven-publish")
    id("kotlinx-serialization")
    alias(libs.plugins.kmmbridge)
    alias(libs.plugins.buildkonfig)
}

configureCompilerOptions()

dependencies {
    implementation(platform(libs.firebase.bom))
}


kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "confetti"
        browser {
            commonWebpackConfig {
                outputFileName = "confetti.js"
            }
        }
        binaries.executable()
    }

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

    applyDefaultHierarchyTemplate {
        common {
            group("mobile") {
                withIos()
                withAndroidTarget()
            }
        }
    }

    sourceSets {

        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization)
                implementation(libs.atomicfu)
                api(libs.kotlinx.datetime)

                // Needed until we can pull in v1.2 with coroutines//wasm support
                //api(libs.bundles.multiplatform.settings)
                implementation("com.russhwolf:multiplatform-settings:1.1.1") {
                    exclude(group = "com.russhwolf", module = "multiplatform-settings-coroutines")
                }
                api(libs.koin.core)
                implementation(libs.koin.compose.multiplatform)

                api(libs.apollo.runtime)
                api(libs.bundles.apollo)

                api(libs.decompose.decompose)
                api(libs.decompose.extensions.compose)
                api(libs.essenty.lifecycle)

                api(libs.kermit)

                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                api(compose.components.resources)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation(libs.coil3.compose)
                implementation(libs.coil3.network.ktor)
                api(libs.materialkolor)
                api(libs.compose.window.size)
                implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.16.0")

                api(libs.generativeai)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val mobileMain by getting {
            dependencies {
                implementation(libs.firebase.mpp.auth)
                api(libs.apollo.normalized.cache.sqlite)
            }
        }

        iosMain {
            dependsOn(mobileMain)
        }

        androidMain {
            dependsOn(mobileMain)
            dependencies {
                api(project(":proto"))
                api(libs.androidx.lifecycle.viewmodel.ktx)
                implementation(libs.okhttp)
                implementation(libs.okhttp.coroutines)
                implementation(libs.okhttp.logging.interceptor)
                api(libs.coil.base)
                api(libs.koin.android)
                api(libs.koin.compose.multiplatform)
                api(libs.koin.workmanager)
                api(libs.okio)
                implementation(libs.horologist.datalayer)
                implementation(libs.coil.svg)

                implementation(libs.firebase.analytics)
                implementation(libs.compose.navigation)

                api(libs.androidx.work.runtime.ktx)

                // Needed until we can pull in v1.2 with coroutines//wasm support
                //api(libs.multiplatform.settings.datastore)
                api("com.russhwolf:multiplatform-settings-datastore:1.1.1") {
                    exclude(group = "com.russhwolf", module = "multiplatform-settings-coroutines")
                }



                api(libs.androidx.datastore)
                api(libs.androidx.datastore.preferences)


                api("com.mikepenz:multiplatform-markdown-renderer:0.16.0")
            }
        }

        sourceSets.invokeWhenCreated("androidDebug") {
            dependencies {
                implementation(libs.apollo.debug.server)
            }
        }

        jvmMain {
            dependencies {
                // hack to allow use of MainScope() in shared code used by JVM console app
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.okhttp)
                implementation(libs.okhttp.coroutines)
                implementation(libs.apollo.testing)
                implementation(libs.apollo.normalized.cache.sqlite)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.0.0-wasm2")
            }
        }
    }
}

compose.resources {
    publicResClass = true
}

android {
    compileSdk = AndroidSdk.compile

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = AndroidSdk.min
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
            "com.apollographql.apollo.adapter.KotlinxLocalDateTimeAdapter"
        )

        mapScalar(
            "LocalDate",
            "kotlinx.datetime.LocalDate",
            "com.apollographql.apollo.adapter.KotlinxLocalDateAdapter"
        )

        introspection {
            endpointUrl.set("https://confetti-app.dev/graphql")
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

val autoVersion = project.property(
    if (project.hasProperty("AUTO_VERSION")) {
        "AUTO_VERSION"
    } else {
        "LIBRARY_VERSION"
    }
) as String

version = autoVersion

kmmbridge {
    frameworkName.set("ConfettiKit")
    mavenPublishArtifacts()
    spm()
}

addGithubPackagesRepository()

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

buildkonfig {
    packageName = "dev.johnoreilly.confetti"

    val localPropsFile = rootProject.file("local.properties")
    val localProperties = Properties()
    if (localPropsFile.exists()) {
        runCatching {
            localProperties.load(localPropsFile.inputStream())
        }.getOrElse {
            it.printStackTrace()
        }
    }
    defaultConfigs {
        buildConfigField(
            FieldSpec.Type.STRING,
            "GEMINI_API_KEY",
            localProperties["gemini_api_key"]?.toString() ?: ""
        )
    }

}
