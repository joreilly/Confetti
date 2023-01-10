import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
  kotlin("multiplatform")
  id("com.google.cloud.tools.appengine")
}

kotlin {
  jvm()

  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.okhttp)
        implementation(libs.okhttp.coroutines)
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
    setServiceAccountKeyFile(file("../service_account_key.json"))
  }
  deploy {
    projectId = "confetti-349319"
    version = "GCLOUD_CONFIG"
  }
}

tasks.named("appengineStage").dependsOn("fatJar")