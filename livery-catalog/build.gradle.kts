import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Confetti's Livery catalog: conference-styled Remote Compose templates (droidcon, KotlinConf, ...)
// authored as @RemoteComposable functions with rc-players' Compose Multiplatform fork of
// remote-creation-compose, captured to .rc on the JVM (no Android runtime, no emulator), and laid
// out as a Livery catalog source tree under catalog/ that `livery build` publishes.
//
//   ./gradlew :livery-catalog:test                     captures every template and checks it plays
//   ./gradlew :livery-catalog:generateLiveryCatalog    (re)writes catalogs/<series>/designs/**.rc + renders/
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
    // Below the fork: AndroidX's RemoteComposeWriter, for the documents it can't write (AGSL shaders).
    implementation(libs.androidx.remote.creation.core)
    // Playing back (previews, tests): the Compose player and Livery's contract binding.
    implementation(libs.rc.player.compose)
    implementation(projects.liveryContracts)
    implementation(libs.livery.compose)
    implementation(libs.livery.render)
    implementation(compose.desktop.currentOs)
    // Material 3 dynamic schemes, for the renders of a user's dynamic colours.
    implementation(compose.material3)
    implementation(libs.materialkolor)
    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization)

    testImplementation(kotlin("test"))
    testImplementation(libs.livery.client)
}

// One catalog source tree per conference series: catalogs/confetti (the base), and one per conference.
val catalogsDir = layout.projectDirectory.dir("catalogs")

tasks.register<JavaExec>("generateLiveryCatalog") {
    group = "livery"
    description = "Captures every Confetti design to .rc into catalogs/<series>/ and renders previews."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.johnoreilly.confetti.livery.GenerateCatalogKt")
    args(catalogsDir.asFile.absolutePath, layout.projectDirectory.dir("renders").asFile.absolutePath)
}

// Livery's publisher (`livery build`), to build each series into the static layout a channel
// serves: manifest.json, blobs/, assets/, previews/. The livery-templates workflow pushes the
// output to that branch; the app's sduiDebug build reads it from raw.githubusercontent.com.
val liveryCli: Configuration by configurations.creating

dependencies {
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
        val src = catalogsDir.dir(series)
        val out = channelsDir.map { it.dir(series) }
        inputs.dir(src)
        outputs.dir(out)
        // --serial: the commit time when CI passes it, so a rebuild of the same commit is the same
        // catalog; otherwise Livery's default (now). It must only grow per channel.
        val serial = providers.gradleProperty("livery.serial")
        doFirst { out.get().asFile.deleteRecursively() }
        argumentProviders.add(CommandLineArgumentProvider {
            listOf("build", src.asFile.absolutePath, out.get().asFile.absolutePath, "--inspect") +
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
