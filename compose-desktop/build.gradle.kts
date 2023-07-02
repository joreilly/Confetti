import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.4.0"
    application
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("io.github.qdsfdhvh:image-loader:1.5.1")
    implementation("dev.chrisbanes.material3:material3-window-size-class-multiplatform:0.1.0")
    implementation(project(":shared"))
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

compose {
    kotlinCompilerPlugin.set(libs.versions.compose.compiler.get())
}

application {
    mainClass.set("MainKt")
}