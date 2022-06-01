plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.apollographql.apollo3")
    id("com.rickclephas.kmp.nativecoroutines")
}

kotlin {
    android()
    jvm()
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                with(Kotlinx) {
                    implementation(coroutinesCore)
                    api(dateTime)
                }

                api(Deps.multiplatformSettings)
                api(Deps.multiplatformSettingsCoroutines)

                // koin
                with(Koin) {
                    api(core)
                }

                // apollo
                with(Apollo) {
                    api(apolloRuntime)
                    implementation(apolloNormalizedCacheInMemory)
                    implementation(apolloNormalizedCacheSqlite)
                    implementation(adapters)
                }
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(Deps.junit)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }

        val jvmMain by getting {
            dependencies {
                // hack to allow use of MainScope() in shared code used by JVM console app
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.kotlinCoroutines}")
            }
        }
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

apollo {
    packageName.set("dev.johnoreilly.confetti")
    codegenModels.set("operationBased")
    generateSchema.set(true)
    mapScalar("Instant", "kotlinx.datetime.Instant", "com.apollographql.apollo3.adapter.KotlinxInstantAdapter")

    introspection {
        endpointUrl.set("https://confetti-349319.uw.r.appspot.com/graphql")
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

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}
