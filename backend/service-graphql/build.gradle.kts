import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
  kotlin("jvm")
  id("org.jetbrains.kotlin.plugin.spring")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("org.springframework.boot")
  id("com.google.cloud.tools.appengine")
}

dependencies {
  implementation(libs.graphql.kotlin.spring.server)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization)
  implementation(libs.bare.graphQL)
  implementation(project(":backend:datastore"))
  implementation(libs.okhttp)
  implementation(libs.reflect)
  implementation(libs.xoxo)
  implementation(libs.apollo.tooling)

  testImplementation(libs.junit)
}

appengine {
  stage {
    setArtifact(tasks.named("bootJar").flatMap { (it as Jar).archiveFile })
  }
  tools {
    setServiceAccountKeyFile(file("../service_account_key.json"))
  }
  deploy {
    projectId = "confetti-349319"
    version = "GCLOUD_CONFIG"
  }
}

tasks.named("appengineStage").dependsOn("bootJar")