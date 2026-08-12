@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("org.jetbrains.compose")
    alias(libs.plugins.compose.compiler)
}

group = "com.example"
version = "1.0-SNAPSHOT"

kotlin {
    wasmJs {
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
                implementation(projects.shared)
            }
        }
    }
}

dependencies {
    // haze (blur effect, used by :shared) pulls cache4k 0.13.0 -> stately-*-wasm 2.0.6, whose
    // legacy "-wasm" artifacts depend on kotlin-stdlib-wasm:1.9.10. That collides with the
    // project's kotlin-stdlib-wasm-js and fails wasmJs linking with a duplicate klib error.
    // Newer cache4k/stately publish a proper wasmJs variant with no such legacy artifact.
    constraints {
        add("wasmJsRuntimeClasspath", libs.cache4k)
        add("wasmJsRuntimeClasspath", libs.stately.common)
        add("wasmJsRuntimeClasspath", libs.stately.isolate)
        add("wasmJsRuntimeClasspath", libs.stately.iso.collections)
    }
}
