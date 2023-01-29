
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
include(":wearApp")
include(":wearBenchmark")