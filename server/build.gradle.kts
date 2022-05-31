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
  implementation(Deps.okhttp)
  implementation(Kotlin.reflect)
  implementation(Deps.xoxo)

  testImplementation(Deps.junit)
}

appengine {
  stage {
    setArtifact(tasks.named("bootJar").flatMap { (it as Jar).archiveFile })
  }
  deploy {
    projectId = "confetti-349319"
    version = "GCLOUD_CONFIG"
  }
}