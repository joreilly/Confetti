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

        tasks.named<ProcessResources>(compilations["main"].processResourcesTaskName) {
            from(projects.shared.dependencyProject.file("src/commonMain/composeResources"))
        }
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

compose.experimental {
    web.application {}
}
