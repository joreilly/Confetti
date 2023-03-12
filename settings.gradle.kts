
pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            mavenCentral()
            google()
            maven("https://androidx.dev/storage/compose-compiler/repository")
            gradlePluginPortal {
                content {
                }
            }
        }
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                // Appengine plugin doesn't publish the marker
                "com.google.cloud.tools.appengine" -> useModule("com.google.cloud.tools:appengine-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "Confetti"
include(":androidApp")
include(":androidBenchmark")
include(":shared")
include(":backend")
include(":backend:service-graphql")
include(":backend:datastore")
include(":backend:service-import")
include(":landing-page")
include(":wearApp")
include(":wearBenchmark")

val javaVersion = System.getProperty("java.version")?.split(".")?.firstOrNull()?.toInt() ?: Int.MAX_VALUE

check (javaVersion >= 17) {
    "This project needs to be run with Java 17 or higher (found: $javaVersion)."
}