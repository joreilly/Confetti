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
  implementation(Apollo.tooling)

  testImplementation(Deps.junit)
}

appengine {
  stage {
    setArtifact(tasks.named("bootJar").flatMap { (it as Jar).archiveFile })
  }
  tools {
    setServiceAccountKeyFile(file("google_services.json"))
  }
  deploy {
    projectId = "confetti-349319"
    version = "GCLOUD_CONFIG"
  }
}

tasks.register("setupGoogleServices") {
  doLast {
    file("google_services.json").writeText(System.getenv("GOOGLE_SERVICES_JSON"))
  }
}