import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.apollographql.apollo3")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(libs.apollo.runtime)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
        }
    }
}

compose.experimental {
    web.application {}
}

apollo {
    service("api") {
        schemaFile.set(file("../shared/src/commonMain/graphql/schema.graphqls"))
        packageName.set("confetti.web")
    }
}