import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Confetti's Wear designs: each conference series' Livery designs as Wear variants (`variant` wear,
// tagged `device=wear`), written with Wear's own Material 3 components as Remote Compose. They use
// rc-players' Compose Multiplatform port of AndroidX's remote-material3 (rc-creation-wear-material3,
// package ee.schimke.composeai.rccreation.wear.material3), which captures on the plain JVM like
// :livery:catalog, with no Android or Robolectric, and the same creation API (rc-creation-compose).
//
// captureWearDesigns writes each series' Wear designs into build/livery/wear/<series>/designs/, and
// :livery:catalog's assembleLiveryCatalogs adds them to that series' catalog.
plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(platform(libs.rc.players.bom))
    implementation(libs.rc.creation.wear.material3)
    implementation(libs.rc.remote.creation.compose)
    implementation(compose.desktop.currentOs)
    implementation(platform(libs.livery.bom))
    implementation(libs.livery.capture.core)
    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization)
}

// The JVM capture's tree for each series (the contracts and theme packs these designs read), and
// where this one writes.
val capturedCatalogs = rootProject.layout.projectDirectory.dir("livery/catalog/build/livery/captured")
val wearDesigns = layout.buildDirectory.dir("livery/wear")

tasks.register<JavaExec>("captureWearDesigns") {
    group = "livery"
    description = "Captures each series' Wear designs into build/livery/wear/<series>/designs/."
    dependsOn(":livery:catalog:captureLiveryCatalog")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.johnoreilly.confetti.livery.wear.CaptureWearDesignsKt")
    // Locals, not the script's values: the configuration cache can't hold a script reference.
    val captured = capturedCatalogs.asFile
    val out = wearDesigns.get().asFile
    inputs.dir(captured).withPropertyName("captured")
    outputs.dir(out).withPropertyName("wearDesigns")
    outputs.cacheIf { true }
    doFirst { out.deleteRecursively() }
    argumentProviders.add(CommandLineArgumentProvider { listOf(captured.absolutePath, out.absolutePath) })
}
