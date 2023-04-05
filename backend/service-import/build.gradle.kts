import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
  kotlin("multiplatform")
  id("com.google.cloud.tools.appengine")
  id("org.jetbrains.kotlin.plugin.serialization")
}

configureCompilerOptions(17)

kotlin {
  jvm()

  sourceSets {
    val jvmMain by getting {
      dependencies {
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

        implementation(project(":backend:datastore"))
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

val fatJar = tasks.register("fatJar", Jar::class.java) {
  manifest {
    attributes(mapOf("Main-Class" to "dev.johnoreilly.confetti.backend.import.MainKt"))
  }

  // we need flatMap here to avoid an obscure error of resolving the classpath too early
  val fileCollection = configurations.named("jvmRuntimeClasspath").flatMap {
    provider {
      it.files.map {
        if (it.isDirectory) it else zipTree(it)
      }
    }
  }
  from(fileCollection) {
    // Exclude duplicates
    exclude(
      "META-INF/*.SF",
      "META-INF/*.RSA",
      "META-INF/*.DSA",
      "META-INF/versions/9/module-info.class",
      "META-INF/INDEX.LIST",
      "META-INF/LICENSE.txt",
      "META-INF/LICENSE",
      "META-INF/NOTICE.txt",
      "META-INF/NOTICE",
      "META-INF/DEPENDENCIES",
      "META-INF/services/io.grpc.NameResolverProvider",
      "META-INF/services/io.grpc.LoadBalancerProvider",
      /**
       * auto-value 1.10.1 duplicates all those below
       */
      "META-INF/kotlin-stdlib.kotlin_module",
      "META-INF/maven/com.google.errorprone/error_prone_annotations/pom.properties",
      "META-INF/maven/com.google.errorprone/error_prone_annotations/pom.xml",
      "META-INF/maven/com.google.guava/failureaccess/pom.properties",
      "META-INF/maven/com.google.guava/failureaccess/pom.xml",
      "META-INF/maven/com.google.guava/listenablefuture/pom.properties",
      "META-INF/maven/com.google.guava/listenablefuture/pom.xml",
      "META-INF/maven/com.google.j2objc/j2objc-annotations/pom.properties",
      "META-INF/maven/com.google.j2objc/j2objc-annotations/pom.xml",
      "META-INF/maven/org.jetbrains/annotations/pom.properties",
      "META-INF/maven/org.jetbrains/annotations/pom.xml",
      "META-INF/metadata.jvm.kotlin_module",
      "META-INF/metadata.kotlin_module",
      "META-INF/maven/com.google.guava/guava/pom.properties",
      "META-INF/maven/com.google.guava/guava/pom.xml",
      "META-INF/native-image/native-image.properties",

    )
  }
  with(tasks.getByName("jvmJar") as CopySpec)
  archiveClassifier.set("all")
}

val localRun = tasks.register("localRun", JavaExec::class.java) {
  classpath(fatJar)
}

appengine {
  stage {
    setArtifact(fatJar.flatMap { it.archiveFile })
  }
  tools {
    setServiceAccountKeyFile(gcpServiceAccountFile())
  }
  deploy {
    projectId = "confetti-349319"
    version = "GCLOUD_CONFIG"
  }
}

tasks.named("appengineStage").dependsOn("fatJar")