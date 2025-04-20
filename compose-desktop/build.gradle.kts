import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()

    sourceSets {
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)

            implementation(libs.decompose.decompose)
            implementation(libs.decompose.extensions.compose)

            implementation(project(":shared"))
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

compose.desktop {
    application {
        mainClass = "MainKt"
        //jvmArgs.add("-Duser.language=de")
    }
}
