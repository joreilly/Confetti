plugins {
  kotlin("multiplatform")
}

kotlin {
  jvm()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(Kotlinx.dateTime)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation("com.google.cloud:google-cloud-datastore:2.11.0")
        implementation(Deps.bareGraphQL)
        implementation(Kotlinx.serialization)
      }
    }
  }
}