plugins {
  id("org.jetbrains.kotlin.jvm")
  id("com.google.cloud.tools.appengine")
  id("com.google.devtools.ksp")
  id("com.apollographql.apollo3")
}

configureCompilerOptions(17)

dependencies {
  implementation(libs.kotlinx.datetime)
  implementation(libs.apollo.annotations)
  implementation(libs.apollo.ast)
  implementation(libs.apollo.api)
  implementation(libs.apollo.execution)
  implementation(libs.apollo.adapters)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.atomicfu)

  implementation(project(":backend:datastore"))
  implementation(libs.okhttp)
  implementation(libs.apollo.tooling) // For uploading the schema
  implementation(libs.apollo.runtime)
  implementation(libs.firebase.admin)
  implementation(platform(libs.http4k.bom.get()))
  implementation(libs.http4k.server.undertow)
  implementation(libs.http4k.core)

  testImplementation(kotlin("test"))
  add("ksp", apollo.apolloKspProcessor(file("src/main/resources/schema.graphqls"), "confetti", "confetti"))
}

appengine {
  stage {
    setArtifact(tasks.named("jar").flatMap { (it as Jar).archiveFile })
  }
  tools {
    setServiceAccountKeyFile(gcpServiceAccountFile())
  }
  deploy {
    projectId = "confetti-349319"
    version = "GCLOUD_CONFIG"
  }
}

//tasks.named("appengineStage").dependsOn("bootJar")

tasks.register("runServer", JavaExec::class) {
  classpath(configurations.getByName("runtimeClasspath"))
  classpath(tasks.named("jar"))
  mainClass.set("dev.johnoreilly.confetti.backend.MainKt")
}
