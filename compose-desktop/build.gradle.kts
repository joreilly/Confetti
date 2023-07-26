import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version libs.versions.compose.multiplatform
    application
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.image.loader)
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