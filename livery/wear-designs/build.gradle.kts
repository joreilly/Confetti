import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Confetti's Wear designs: each conference series' Livery designs as Wear variants (`variant` wear,
// tagged `device=wear`), written with Wear's own Material 3 components as Remote Compose. They use
// yschimke/remote-m3-catalog's multiplatform port of AndroidX's remote-material3 (vendor/ there),
// which captures on the plain JVM like :livery:catalog, with no Android or Robolectric. That port
// is in AndroidX's package rather than rc-players', so it is a module of its own, with its own
// contract-checked values (WearValues) over livery-capture-core.
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

repositories {
    // A module's own repositories replace the settings' ones, so those first.
    google()
    mavenCentral()
    // The port, and the Wear Compose multiplatform port it uses for Wear's token types on the JVM:
    // credential-free Maven trees on branches of their output repositories. Only for their groups.
    exclusiveContent {
        forRepository { maven("https://raw.githubusercontent.com/yschimke/remote-m3-catalog-out/remote-compose-cmp-maven/") }
        filter { includeGroup("ee.schimke.remotecompose") }
    }
    exclusiveContent {
        forRepository { maven("https://raw.githubusercontent.com/yschimke/wear-m3-catalog-out/wear-compose-cmp-maven/") }
        filter { includeGroup("ee.schimke.wearcmp") }
    }
}

dependencies {
    implementation(libs.remote.compose.cmp.material3)
    implementation(libs.remote.compose.cmp.creation.compose)
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
