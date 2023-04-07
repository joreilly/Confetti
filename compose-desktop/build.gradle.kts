import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.4.0-rc03"
    application
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("io.github.qdsfdhvh:image-loader:1.3.1")
    implementation(project(":shared"))
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "17"
    }
}


compose {
    kotlinCompilerPlugin.set(libs.versions.compose.compiler.get())
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=1.8.20")
}

application {
    mainClass.set("MainKt")
}