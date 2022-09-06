import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
  kotlin("jvm")
  id("org.jetbrains.kotlin.plugin.spring")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("org.springframework.boot")
  id("com.google.cloud.tools.appengine")
}

dependencies {
  implementation(Deps.graphqlKotlinSpringServer)
  implementation(Kotlinx.dateTime)
  implementation(Kotlinx.serialization)
  implementation(Deps.bareGraphQL)
  implementation(project(":backend:datastore"))
  implementation(Deps.okhttp)
  implementation(Kotlin.reflect)
  implementation(Deps.xoxo)
  implementation(Apollo.tooling)

  testImplementation(Deps.junit)
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