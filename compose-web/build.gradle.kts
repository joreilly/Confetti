import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("org.jetbrains.compose")
    alias(libs.plugins.compose.compiler)
}

group = "com.example"
version = "1.0-SNAPSHOT"


val copyWasmResources = tasks.create("copyWasmResourcesWorkaround", Copy::class.java) {
    from(project(":shared").file("src/commonMain/composeResources"))
    into("build/processedResources/wasmJs/main")
}


afterEvaluate {
    project.tasks.getByName("wasmJsProcessResources").finalizedBy(copyWasmResources)
    project.tasks.getByName("wasmJsDevelopmentExecutableCompileSync").dependsOn(copyWasmResources)
    project.tasks.getByName("wasmJsProductionExecutableCompileSync").dependsOn(copyWasmResources)
}

kotlin {
    wasmJs {
        moduleName = "confetti"
        browser {
            commonWebpackConfig {
                outputFileName = "confetti.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(project(":shared"))
            }
        }
    }
}

compose.experimental {
    web.application {}
}

tasks.configureEach {
    if (name == "wasmJsJar") {
        val wasmJsJar = this
        tasks.configureEach {
            if (name == "copyWasmResourcesWorkaround") {
                wasmJsJar.dependsOn(this)
            }
        }
    }
}