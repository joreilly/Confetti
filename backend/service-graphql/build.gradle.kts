import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.kotlin.plugin.spring")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("org.springframework.boot")
  id("com.google.devtools.ksp")
  id("com.apollographql.execution")
}

configureCompilerOptions(17)

dependencies {
  implementation(libs.spring.boot.starter.webflux)
  implementation(libs.apollo.execution.spring)
  implementation(libs.apollo.execution.reporting)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization)
  implementation(libs.bare.graphQL)
  implementation(projects.backend.datastore)
  implementation(libs.okhttp)
  implementation(libs.reflect)
  implementation(libs.xoxo)
  implementation(libs.apollo.tooling)
  implementation(libs.apollo.annotations)
  implementation(libs.firebase.admin)
  implementation(libs.kotlinx.coroutines.reactor)

  implementation(libs.scrimage.core)
  implementation(libs.scrimage.filters)

  testImplementation(libs.junit)
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.jvmTarget = "17"
}

springBoot {
  mainClass.set("dev.johnoreilly.confetti.backend.MainKt")
}

apolloExecution {
  service("service") {
    schemaFile.set(file("../../shared/src/commonMain/graphql/schema.graphqls"))
  }
}
configureDeploy("graphql", "dev.johnoreilly.confetti.backend.MainKt")

tasks.configureEach {
  if (name == "kspTestKotlin") {
    /**
     * Running KSP on tests fails with '[ksp] No '@GraphQLQuery' class found'
     * Disable that
     */
    enabled = false
  }
}
