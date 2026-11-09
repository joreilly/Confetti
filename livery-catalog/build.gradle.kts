import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Confetti's Livery catalog: conference-styled Remote Compose templates (droidcon, KotlinConf, ...)
// authored as @RemoteComposable functions with rc-players' Compose Multiplatform fork of
// remote-creation-compose, captured to .rc on the JVM (no Android runtime, no emulator).
//
// The source is always Kotlin. A .rc is build output, or lives on the livery-templates branch the
// workflow publishes; it is never committed. catalogs/<series>/ holds only the hand-edited data
// (themes/, placements/, experiments/, fonts/).
//
//   ./gradlew :livery-catalog:test                     captures every template and checks it plays
//   ./gradlew :livery-catalog:generateLiveryCatalog    each series' catalog into build/livery/catalogs/<series>/
//   ./gradlew :livery-catalog:renderLiveryCatalog      the previews into build/livery/renders/
//   ./gradlew :livery-catalog:buildLiveryChannels      `livery build --inspect` of each series into
//                                                     build/livery-channels/<series>/, what the
//                                                     livery-templates branch serves
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
    // Authoring: the CMP fork of androidx remote-creation-compose (JVM + Android + iOS).
    implementation(libs.rc.remote.creation.compose)
    // Playing back (previews, tests): the Compose player and Livery's contract binding.
    implementation(libs.rc.player.compose)
    implementation(projects.liveryContracts)
    implementation(platform(libs.livery.bom))
    implementation(libs.livery.compose)
    implementation(libs.livery.render)
    implementation(libs.livery.compose.material3)
    implementation(compose.desktop.currentOs)
    // Material 3 dynamic schemes, for the renders of a user's dynamic colours.
    implementation(compose.material3)
    implementation(libs.materialkolor)
    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization)

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.livery.bom))
    testImplementation(libs.livery.client)
}

// One catalog per conference series: catalogs/confetti (the base), and one per conference. Only the
// hand-edited data is here; the generated catalogs are in build/livery/catalogs.
val catalogsDir = layout.projectDirectory.dir("catalogs")
val generatedCatalogsDir = layout.buildDirectory.dir("livery/catalogs")

val generateLiveryCatalog = tasks.register<JavaExec>("generateLiveryCatalog") {
    group = "livery"
    description = "Captures every Confetti design to .rc, with each series' data, into build/livery/catalogs/<series>/."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.johnoreilly.confetti.livery.GenerateCatalogKt")
    // Locals, not the script's vals: the configuration cache can't serialize script references.
    val source = catalogsDir.asFile.absolutePath
    val out = generatedCatalogsDir
    inputs.dir(catalogsDir)
    outputs.dir(out)
    outputs.cacheIf { true }
    argumentProviders.add(CommandLineArgumentProvider { listOf(source, out.get().asFile.absolutePath) })
}

tasks.register<JavaExec>("renderLiveryCatalog") {
    group = "livery"
    description = "Renders every Confetti design's previews into build/livery/renders/."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.johnoreilly.confetti.livery.GenerateCatalogKt")
    val source = catalogsDir.asFile.absolutePath
    val catalogs = layout.buildDirectory.dir("livery/render-catalogs")
    val renders = layout.buildDirectory.dir("livery/renders")
    inputs.dir(catalogsDir)
    outputs.dirs(catalogs, renders)
    outputs.cacheIf { true }
    doFirst { renders.get().asFile.deleteRecursively() }
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(source, catalogs.get().asFile.absolutePath, renders.get().asFile.absolutePath)
    })
}

// Livery's publisher (`livery build`), to build each series into the static layout a channel
// serves: manifest.json, blobs/, assets/, previews/. The livery-templates workflow pushes the
// output to that branch; the app's sduiDebug build reads it from raw.githubusercontent.com.
val liveryCli: Configuration by configurations.creating

dependencies {
    liveryCli(platform(libs.livery.bom))
    liveryCli(libs.livery.publish)
}

val channelsDir = layout.buildDirectory.dir("livery-channels")

val buildLiveryChannels = tasks.register("buildLiveryChannels") {
    group = "livery"
    description = "Builds every series catalog with `livery build --inspect` into build/livery-channels/<series>/."
}

catalogsDir.asFile.listFiles { f -> f.isDirectory }.orEmpty().map { it.name }.sorted().forEach { series ->
    val task = tasks.register<JavaExec>("buildLiveryChannel${series.replaceFirstChar { it.uppercase() }}") {
        group = "livery"
        classpath = liveryCli
        mainClass.set("ee.schimke.livery.publisher.MainKt")
        val src = generatedCatalogsDir.map { it.dir(series) }
        val out = channelsDir.map { it.dir(series) }
        dependsOn(generateLiveryCatalog)
        inputs.dir(src)
        outputs.dir(out)
        // --serial: the commit time when CI passes it, so a rebuild of the same commit is the same
        // catalog; otherwise Livery's default (now). It must only grow per channel.
        val serial = providers.gradleProperty("livery.serial")
        doFirst { out.get().asFile.deleteRecursively() }
        argumentProviders.add(CommandLineArgumentProvider {
            listOf("build", src.get().asFile.absolutePath, out.get().asFile.absolutePath, "--inspect") +
                (serial.orNull?.let { listOf("--serial", it) } ?: emptyList())
        })
    }
    buildLiveryChannels.configure { dependsOn(task) }
}

// ShaderExperimentTest reads the KotlinConf channel as the publisher builds it, to check what
// Livery's own selector picks from it (the shader template, its fallback, the default design).
tasks.named<Test>("test") {
    dependsOn("buildLiveryChannelKotlinconf")
    systemProperty("livery.channels", channelsDir.get().asFile.absolutePath)
}
