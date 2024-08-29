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


tasks.withType<org.jetbrains.kotlin.gradle.targets.js.binaryen.BinaryenExec>().configureEach {
    binaryenArgs = mutableListOf(
        // Proposals
        "--enable-gc",
        "--enable-reference-types",
        "--enable-exception-handling",
        "--enable-bulk-memory",  // For array initialization from data sections

        // Other options
        "--enable-nontrapping-float-to-int",
        "--closed-world",

        // Optimizations:
        // Note the order and repetition of the next options matter.
        //
        // About Binaryen optimizations:
        // GC Optimization Guidebook -- https://github.com/WebAssembly/binaryen/wiki/GC-Optimization-Guidebook
        // Optimizer Cookbook -- https://github.com/WebAssembly/binaryen/wiki/Optimizer-Cookbook
        //
        "--inline-functions-with-loops",
        "--traps-never-happen",
        "--fast-math",
        // without "--type-merging" it produces increases the size
        "--type-ssa",
        "-O3",
        "-O3",
        "--gufa",
        "-O3",
        // requires --closed-world
        "--type-merging",
        "-O3",
        "-Oz",
    )
}