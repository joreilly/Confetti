import com.android.build.gradle.internal.tasks.factory.dependsOn
import compat.patrouille.configureJavaCompatibility

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.kotlin.plugin.serialization")
}

configureJavaCompatibility(17)

kotlin {
  jvm()

  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.jsonpath)
        implementation(libs.jsoup)
        implementation(libs.okhttp)
        implementation(libs.okhttp.coroutines)
        implementation(libs.okhttp.logging.interceptor)
        implementation(libs.xoxo)
        implementation(libs.ktor.cio)
        implementation(libs.ktor.status.pages)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kaml)
        implementation(libs.bare.graphQL)
        implementation(libs.kotlinx.serialization)
        implementation(libs.kotlin.csv)
        implementation(projects.backend.datastore)
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(libs.junit)
      }
    }
  }
}

abstract class GenerateApiKey : DefaultTask() {
  @get:Input
  abstract val apiKey: Property<String>

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @TaskAction
  fun taskAction() {
    val versionFile = File(outputDir.asFile.get(), "ApiKey.kt")
    versionFile.parentFile.mkdirs()
    versionFile.writeText("""// Generated file. Do not edit!
package dev.johnoreilly.confetti
const val DEEPAI_API_KEY = "${apiKey.get()}"
""")
  }
}

fun getApiKey(): String {
  var apiKey = System.getenv("DEEPAI_API_KEY")
  if (!apiKey.isNullOrBlank()) {
    return apiKey
  }

  val file = file("deepai.key")
  if (file.exists() && file.isFile) {
    apiKey = file("deepai.key").readText().trim()
    if (!apiKey.isNullOrBlank()) {
      return apiKey
    }
  }

  return "Placeholder (use DEEPAI_API_KEY or deepai.key to replace)"
}

val generateApiKey = tasks.register("generateApiKey", GenerateApiKey::class.java) {
  outputDir.set(project.layout.buildDirectory.dir("generated/kotlin/deepai"))
  apiKey.set(getApiKey())
}

kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(generateApiKey)

val localRun = tasks.register("localRun", JavaExec::class.java) {
  classpath(configurations.named("jvmRuntimeClasspath"))
  classpath(tasks.named("jvmJar"))
  mainClass.set("dev.johnoreilly.confetti.backend.import.MainKt")
}

configureDeploy("import", "dev.johnoreilly.confetti.backend.import.MainKt")
