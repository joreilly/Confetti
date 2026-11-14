// The sessions list, flung on the desktop JVM: Confetti's own SessionItemView against the same
// sessions drawn by Livery templates, frame by frame. Not part of any app; run it with
//   ./gradlew :livery:benchmark:sessionsBenchmark -Pconfetti.benchmark.catalogs=<dir>
// where <dir> holds one Livery catalog per series (<dir>/<series>/manifest.json, blobs/, assets/):
// the livery-templates branch's catalogs/ directory, for instance. See livery/benchmark/README.md.
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            @Suppress("DEPRECATION") implementation(compose.desktop.currentOs)
            @Suppress("DEPRECATION") implementation(compose.material3)
            implementation(projects.shared)
            implementation(projects.livery.contracts)
            // The same BOMs the app's :livery:android plays templates with.
            implementation(project.dependencies.platform(libs.livery.bom))
            implementation(project.dependencies.platform(libs.rc.players.bom))
            implementation(libs.livery.client)
            implementation(libs.livery.compose.asProvider())
        }
    }
}

val catalogs = providers.gradleProperty("confetti.benchmark.catalogs")

tasks.register<JavaExec>("sessionsBenchmark") {
    description = "Fling the sessions list: Confetti's SessionItemView against Livery's session cards."
    group = "verification"
    val jvm = kotlin.jvm().compilations.getByName("main")
    classpath = files(jvm.output.allOutputs, jvm.runtimeDependencyFiles)
    mainClass.set("dev.johnoreilly.confetti.livery.benchmark.SessionsBenchmarkKt")
    args(layout.buildDirectory.dir("benchmark").get().asFile.absolutePath, catalogs.orNull ?: "")
    // A measurement, not a build output: never up to date.
    outputs.upToDateWhen { false }
}
