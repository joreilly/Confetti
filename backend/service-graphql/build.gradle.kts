import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.kotlin.plugin.spring")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("org.springframework.boot")
}

configureCompilerOptions(17)

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
  implementation(libs.apollo.annotations)
  implementation(libs.firebase.admin)
  implementation(libs.federation.jvm)

  implementation(libs.scrimage.core)
  implementation(libs.scrimage.filters)
  implementation(libs.spring.web)
  implementation(libs.snakeyaml)

  testImplementation(libs.junit)
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.jvmTarget = "17"
}

springBoot {
  mainClass.set("dev.johnoreilly.confetti.backend.MainKt")
}

tasks.register("updateSchema", JavaExec::class) {
  classpath(configurations.getByName("runtimeClasspath"))
  classpath(tasks.named("jar"))
  mainClass.set("dev.johnoreilly.confetti.backend.UpdateSchemaKt")
}

configureDeploy("graphql", "dev.johnoreilly.confetti.backend.MainKt")